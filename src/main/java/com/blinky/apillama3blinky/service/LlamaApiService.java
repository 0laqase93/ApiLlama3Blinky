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
import java.util.Optional;

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
    public PromptResponse sendPrompt(PromptDTO promptDTO) {
        User user = findUserById(promptDTO.getUserId());
        Conversation conversation = getOrCreateConversation(user);

        Personality personality = getPersonalityForPrompt(promptDTO, conversation);

        UserMessage userMessage = addUserMessageToConversation(conversation, promptDTO.getPrompt());
        OllamaResponse iaResponse = processPromptWithHistory(conversation, personality);
        addAssistantMessageToConversation(conversation, iaResponse.getResponse(), userMessage, personality);

        conversationRepository.save(conversation);
        return PromptMapping.mapToResponse(iaResponse);
    }

    /**
     * Gets the personality to use for a prompt, using the cache to avoid database queries.
     *
     * @param promptDTO the prompt DTO containing the personality ID
     * @param conversation the conversation
     * @return the personality to use
     * @throws ResourceNotFoundException if no personality is found
     */
    private Personality getPersonalityForPrompt(PromptDTO promptDTO, Conversation conversation) {
        // If a personality ID is specified in the prompt, use it
        if (promptDTO.getPersonalityId() != null) {
            // Get the personality from the cache instead of querying the database
            return personalityCache.getPersonalityById(promptDTO.getPersonalityId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "La personalidad especificada no existe. Por favor, verifica el ID proporcionado."));
        }

        // If the conversation has previous responses, use the personality from the last response
        if (!conversation.getAiResponses().isEmpty()) {
            AIResponse lastResponse = conversation.getAiResponses().get(conversation.getAiResponses().size() - 1);
            if (lastResponse.getPersonality() != null) {
                return lastResponse.getPersonality();
            }
        }

        // Otherwise, use the first personality in the cache
        return personalityCache.getFirstPersonality()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró ninguna personalidad. Por favor, crea al menos una personalidad."));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    /**
     * Gets an existing conversation for a user or creates a new one if none exists.
     * Uses fetch join to load all messages and responses in a single query.
     *
     * @param user the user
     * @return the conversation with all messages and responses loaded
     */
    private Conversation getOrCreateConversation(User user) {
        Conversation conversation = user.getConversation();
        if (conversation == null) {
            conversation = initializeNewConversation(user);
        } else {
            // Use the fetch join method to load all messages and responses in a single query
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

    /**
     * Processes a prompt with conversation history, using a sliding window approach.
     * 
     * @param conversation the conversation containing the history
     * @param personality the personality to use for the response
     * @return the response from the AI model
     */
    private OllamaResponse processPromptWithHistory(Conversation conversation, Personality personality) {
        String basePrompt = personality.getBasePrompt();
        // Use a window size of 8 as specified in the requirements
        String historyPrompt = buildPromptWithWindow(conversation, 8);
        String fullPrompt = basePrompt + "\n\n" + historyPrompt;

        OllamaDTO ollamaDTO = OllamaMapping.toOllamaDTO(new PromptDTO(fullPrompt), iaModel);
        return iaService.sendPrompt(ollamaDTO);
    }

    /**
     * Builds a prompt with the last N interactions from the conversation history.
     * This implements a sliding window approach to limit the number of tokens sent to the model.
     *
     * @param conversation the conversation containing the history
     * @param windowSize the maximum number of interactions to include
     * @return a string containing the formatted conversation history
     */
    private String buildPromptWithWindow(Conversation conversation, int windowSize) {
        StringBuilder sb = new StringBuilder();
        List<UserMessage> userMessages = conversation.getUserMessages();

        // Calculate the starting index to get only the last windowSize messages
        int startIndex = Math.max(0, userMessages.size() - windowSize);

        // Process only the messages within the window
        for (int i = startIndex; i < userMessages.size(); i++) {
            UserMessage userMessage = userMessages.get(i);
            sb.append(USER_ROLE).append(": ").append(userMessage.getContent()).append("\n");

            // Find the AI response for this user message from the conversation's aiResponses list
            // This avoids making a separate database query for each message
            conversation.getAiResponses().stream()
                    .filter(ar -> ar.getUserMessage() != null && ar.getUserMessage().getId().equals(userMessage.getId()))
                    .findFirst()
                    .ifPresent(aiResponse ->
                            sb.append(ASSISTANT_ROLE).append(": ").append(aiResponse.getContent()).append("\n"));
        }

        return sb.toString();
    }

    /**
     * @deprecated Use buildPromptWithWindow instead to limit the number of tokens
     */
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
