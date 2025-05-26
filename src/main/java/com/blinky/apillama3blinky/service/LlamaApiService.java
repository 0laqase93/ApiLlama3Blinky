package com.blinky.apillama3blinky.service;

import com.blinky.apillama3blinky.cache.PersonalityCache;
import com.blinky.apillama3blinky.controller.dto.OllamaDTO;
import com.blinky.apillama3blinky.controller.dto.PromptDTO;
import com.blinky.apillama3blinky.controller.response.OllamaResponse;
import com.blinky.apillama3blinky.controller.response.PromptResponse;
import com.blinky.apillama3blinky.exception.ResourceNotFoundException;
import com.blinky.apillama3blinky.mapping.OllamaMapping;
import com.blinky.apillama3blinky.mapping.PromptMapping;
import com.blinky.apillama3blinky.model.*;
import com.blinky.apillama3blinky.repository.AIResponseRepository;
import com.blinky.apillama3blinky.repository.ConversationRepository;
import com.blinky.apillama3blinky.repository.UserMessageRepository;
import com.blinky.apillama3blinky.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LlamaApiService {
    private static final String USER_ROLE = "user";
    private static final String ASSISTANT_ROLE = "assistant";
    private static final String DEFAULT_CONVERSATION_NAME_TEMPLATE = "Blinky Conversation: %s";

    @Value("${ia.model}")
    private String iaModel;

    private final OllamaService iaService;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final UserMessageRepository userMessageRepository;
    private final AIResponseRepository aiResponseRepository;
    private final PersonalityService personalityService;
    private final PersonalityCache personalityCache;

    public LlamaApiService(OllamaService iaService,
                           UserRepository userRepository,
                           ConversationRepository conversationRepository,
                           UserMessageRepository userMessageRepository,
                           AIResponseRepository aiResponseRepository,
                           PersonalityService personalityService,
                           PersonalityCache personalityCache) {
        this.iaService = iaService;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.userMessageRepository = userMessageRepository;
        this.aiResponseRepository = aiResponseRepository;
        this.personalityService = personalityService;
        this.personalityCache = personalityCache;
    }

    @Transactional
    public PromptResponse sendPrompt(PromptDTO promptDTO, Long userId) {
        User user = findUserById(userId);
        Conversation conversation = getOrCreateConversation(user);

        Personality personality = getPersonalityForPrompt(promptDTO, conversation);

        UserMessage userMessage = addUserMessageToConversation(conversation, promptDTO.getPrompt());
        OllamaResponse iaResponse = processPromptWithHistory(conversation, personality);
        addAssistantMessageToConversation(conversation, iaResponse.getResponse(), userMessage, personality);

        conversationRepository.save(conversation);
        return PromptMapping.mapToResponse(iaResponse);
    }

    private Personality getPersonalityForPrompt(PromptDTO promptDTO, Conversation conversation) {
        if (promptDTO.getPersonalityId() != null) {
            return personalityCache.getPersonalityById(promptDTO.getPersonalityId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "La personalidad especificada no existe. Por favor, verifica el ID proporcionado."));
        }

        if (!conversation.getAiResponses().isEmpty()) {
            AIResponse lastResponse = conversation.getAiResponses().get(conversation.getAiResponses().size() - 1);
            if (lastResponse.getPersonality() != null) {
                return lastResponse.getPersonality();
            }
        }

        return personalityCache.getFirstPersonality()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró ninguna personalidad. Por favor, crea al menos una personalidad."));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Conversation getOrCreateConversation(User user) {
        Conversation conversation = user.getConversation();
        if (conversation == null) {
            conversation = initializeNewConversation(user);
        } else {
            conversation = conversationRepository.findWithMessagesAndResponses(conversation.getId())
                    .orElse(conversation);
        }
        return conversation;
    }

    private Conversation initializeNewConversation(User user) {
        Conversation conversation = new Conversation(String.format(DEFAULT_CONVERSATION_NAME_TEMPLATE, user.getEmail()));
        conversation.setUser(user);

        return conversationRepository.save(conversation);
    }

    private UserMessage addUserMessageToConversation(Conversation conversation, String content) {
        UserMessage newUserMessage = new UserMessage(content);
        newUserMessage.setConversation(conversation);
        conversation.getUserMessages().add(newUserMessage);
        return userMessageRepository.save(newUserMessage);
    }

    private AIResponse addAssistantMessageToConversation(Conversation conversation, String content, UserMessage userMessage, Personality personality) {
        AIResponse aiResponse = new AIResponse(content);
        aiResponse.setConversation(conversation);
        aiResponse.setUserMessage(userMessage);
        aiResponse.setPersonality(personality);
        conversation.getAiResponses().add(aiResponse);
        return aiResponseRepository.save(aiResponse);
    }

    private OllamaResponse processPromptWithHistory(Conversation conversation, Personality personality) {
        String basePrompt = personality.getBasePrompt();
        // Use a window size of 8 as specified in the requirements
        String historyPrompt = buildPromptWithWindow(conversation, 8);
        String fullPrompt = basePrompt + "\n\n" + historyPrompt;

        OllamaDTO ollamaDTO = OllamaMapping.toOllamaDTO(new PromptDTO(fullPrompt), iaModel);
        return iaService.sendPrompt(ollamaDTO);
    }

    private String buildPromptWithWindow(Conversation conversation, int windowSize) {
        StringBuilder sb = new StringBuilder();
        List<UserMessage> userMessages = conversation.getUserMessages();

        int startIndex = Math.max(0, userMessages.size() - windowSize);

        for (int i = startIndex; i < userMessages.size(); i++) {
            UserMessage userMessage = userMessages.get(i);
            sb.append(USER_ROLE).append(": ").append(userMessage.getContent()).append("\n");

            conversation.getAiResponses().stream()
                    .filter(ar -> ar.getUserMessage() != null && ar.getUserMessage().getId().equals(userMessage.getId()))
                    .findFirst()
                    .ifPresent(aiResponse ->
                            sb.append(ASSISTANT_ROLE).append(": ").append(aiResponse.getContent()).append("\n"));
        }

        return sb.toString();
    }

    @Deprecated
    private String buildPromptWithNewModel(Conversation conversation) {
        return buildPromptWithWindow(conversation, conversation.getUserMessages().size());
    }

    @Transactional
    public void clearConversation(String userId) {
        User user = findUserById(Long.parseLong(userId));
        Conversation conversation = user.getConversation();

        if (conversation != null) {
            conversation.getUserMessages().clear();
            conversation.getAiResponses().clear();

            conversationRepository.save(conversation);
        }
    }
}
