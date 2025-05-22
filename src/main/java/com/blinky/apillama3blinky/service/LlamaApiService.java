package com.blinky.apillama3blinky.service;

import com.blinky.apillama3blinky.config.PromptConfig;
import com.blinky.apillama3blinky.controller.dto.OllamaDTO;
import com.blinky.apillama3blinky.controller.dto.PromptDTO;
import com.blinky.apillama3blinky.controller.response.OllamaResponse;
import com.blinky.apillama3blinky.controller.response.PromptResponse;
import com.blinky.apillama3blinky.exception.ResourceNotFoundException;
import com.blinky.apillama3blinky.mapping.OllamaMapping;
import com.blinky.apillama3blinky.mapping.PromptMapping;
import com.blinky.apillama3blinky.model.AIResponse;
import com.blinky.apillama3blinky.model.Conversation;
import com.blinky.apillama3blinky.model.Personality;
import com.blinky.apillama3blinky.model.User;
import com.blinky.apillama3blinky.model.UserMessage;
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
    private static final String SYSTEM_ROLE = "system";
    private static final String USER_ROLE = "user";
    private static final String ASSISTANT_ROLE = "assistant";
    private static final String DEFAULT_CONVERSATION_NAME_TEMPLATE = "Blinky Conversation: %s";
    private static final String STARTER_PROMPT_NAME = "StarterBlinky";

    @Value("${ia.model}")
    private String iaModel;

    private final OllamaService iaService;
    private final PromptService promptService;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final UserMessageRepository userMessageRepository;
    private final AIResponseRepository aiResponseRepository;
    private final PersonalityService personalityService;

    public LlamaApiService(OllamaService iaService,
                           PromptService promptService,
                           UserRepository userRepository,
                           ConversationRepository conversationRepository,
                           UserMessageRepository userMessageRepository,
                           AIResponseRepository aiResponseRepository,
                           PersonalityService personalityService) {
        this.iaService = iaService;
        this.promptService = promptService;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.userMessageRepository = userMessageRepository;
        this.aiResponseRepository = aiResponseRepository;
        this.personalityService = personalityService;
    }

    @Transactional
    public PromptResponse sendPrompt(PromptDTO promptDTO) {
        System.out.println(promptDTO.getUserId());
        User user = findUserById(promptDTO.getUserId());
        Conversation conversation = getOrCreateConversation(user);

        // Get the personality from the DTO or use the last one from the conversation
        Personality personality = getPersonalityForPrompt(promptDTO, conversation);

        UserMessage userMessage = addUserMessageToConversation(conversation, promptDTO.getPrompt());
        OllamaResponse iaResponse = processPromptWithHistory(conversation, personality);
        addAssistantMessageToConversation(conversation, iaResponse.getResponse(), userMessage, personality);

        conversationRepository.save(conversation);
        return PromptMapping.mapToResponse(iaResponse);
    }

    /**
     * Get the personality for the prompt
     * @param promptDTO the prompt DTO
     * @param conversation the conversation
     * @return the personality to use
     * @throws ResourceNotFoundException if the specified personality doesn't exist or no personalities exist
     */
    private Personality getPersonalityForPrompt(PromptDTO promptDTO, Conversation conversation) {
        // If personality ID is provided in the DTO, use it
        if (promptDTO.getPersonalityId() != null) {
            try {
                return personalityService.getPersonalityById(promptDTO.getPersonalityId());
            } catch (ResourceNotFoundException e) {
                // Rethrow with a more specific message
                throw new ResourceNotFoundException("La personalidad especificada no existe. Por favor, verifica el ID proporcionado.");
            }
        }

        // Otherwise, try to get the last used personality from the conversation
        if (!conversation.getAiResponses().isEmpty()) {
            AIResponse lastResponse = conversation.getAiResponses().get(conversation.getAiResponses().size() - 1);
            if (lastResponse.getPersonality() != null) {
                return lastResponse.getPersonality();
            }
        }

        // If no personality is specified and none was used before, use a default one
        // For now, we'll use the first personality in the database
        List<Personality> personalities = personalityService.getAllPersonalities();
        if (!personalities.isEmpty()) {
            return personalities.get(0);
        }

        // If no personalities exist yet, throw an exception
        throw new ResourceNotFoundException("No se encontró ninguna personalidad. Por favor, crea al menos una personalidad.");
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Conversation getOrCreateConversation(User user) {
        Conversation conversation = user.getConversation();
        if (conversation == null) {
            conversation = initializeNewConversation(user);
        }
        return conversation;
    }

    private Conversation initializeNewConversation(User user) {
        Conversation conversation = new Conversation(String.format(DEFAULT_CONVERSATION_NAME_TEMPLATE, user.getEmail()));
        conversation.setUser(user);

        // Note: In the new model, we don't have a concept of system messages yet
        // This could be added in the future if needed

        return conversationRepository.save(conversation);
    }

    private UserMessage addUserMessageToConversation(Conversation conversation, String content) {
        // Use the new model
        UserMessage newUserMessage = new UserMessage(content);
        newUserMessage.setConversation(conversation);
        conversation.getUserMessages().add(newUserMessage);
        return userMessageRepository.save(newUserMessage);
    }

    private AIResponse addAssistantMessageToConversation(Conversation conversation, String content, UserMessage userMessage, Personality personality) {
        // Use the new model
        AIResponse aiResponse = new AIResponse(content);
        aiResponse.setConversation(conversation);
        aiResponse.setUserMessage(userMessage);
        aiResponse.setPersonality(personality); // Set the personality
        conversation.getAiResponses().add(aiResponse);
        return aiResponseRepository.save(aiResponse);
    }

    private OllamaResponse processPromptWithHistory(Conversation conversation, Personality personality) {
        // Combine the personality's base prompt with the conversation history
        String basePrompt = personality.getBasePrompt();
        String historyPrompt = buildPromptWithNewModel(conversation);
        String fullPrompt = basePrompt + "\n\n" + historyPrompt;

        OllamaDTO ollamaDTO = OllamaMapping.toOllamaDTO(new PromptDTO(fullPrompt), iaModel);
        return iaService.sendPrompt(ollamaDTO);
    }

    // Build the prompt history from the new model
    private String buildPromptWithNewModel(Conversation conversation) {
        StringBuilder sb = new StringBuilder();

        // System messages don't exist in the new model yet
        // This could be added in the future if needed

        // Interleave user messages and AI responses in chronological order
        // This is a simplified approach and might need to be refined based on actual requirements
        for (int i = 0; i < conversation.getUserMessages().size(); i++) {
            UserMessage userMessage = conversation.getUserMessages().get(i);
            sb.append(USER_ROLE).append(": ").append(userMessage.getContent()).append("\n");

            // Find the corresponding AI response
            aiResponseRepository.findByUserMessageId(userMessage.getId())
                    .ifPresent(aiResponse ->
                            sb.append(ASSISTANT_ROLE).append(": ").append(aiResponse.getContent()).append("\n"));
        }

        return sb.toString();
    }

    @Transactional
    public void clearConversation(String userId) {
        User user = findUserById(Long.parseLong(userId));
        Conversation conversation = user.getConversation();

        if (conversation != null) {
            // Clear new model messages
            conversation.getUserMessages().clear();
            conversation.getAiResponses().clear();

            conversationRepository.save(conversation);
        }
    }
}
