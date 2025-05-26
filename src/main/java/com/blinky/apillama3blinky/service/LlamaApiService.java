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

/**
 * Service responsible for handling interactions with the Llama AI model.
 * Manages conversations, user messages, and AI responses.
 */
@Service
public class LlamaApiService {
    // Constants for message roles in the conversation
    private static final String USER_ROLE = "user";
    private static final String ASSISTANT_ROLE = "assistant";
    private static final String DEFAULT_CONVERSATION_NAME_TEMPLATE = "Blinky Conversation: %s";

    // AI model configuration from properties
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

    /**
     * Processes a user prompt and generates an AI response.
     * 
     * @param promptDTO The prompt data transfer object containing the user's message
     * @param userId The ID of the user sending the prompt
     * @return A response containing the AI's reply
     */
    @Transactional
    public PromptResponse sendPrompt(PromptDTO promptDTO, Long userId) {
        // Find the user and get or create their conversation
        User user = findUserById(userId);
        Conversation conversation = getOrCreateConversation(user);

        // Determine which personality to use for this prompt
        Personality personality = getPersonalityForPrompt(promptDTO, conversation);

        // Add the user message to the conversation
        UserMessage userMessage = addUserMessageToConversation(conversation, promptDTO.getPrompt());
        // Process the prompt with conversation history and get AI response
        OllamaResponse iaResponse = processPromptWithHistory(conversation, personality);
        // Add the AI response to the conversation
        addAssistantMessageToConversation(conversation, iaResponse.getResponse(), userMessage, personality);

        // Save the updated conversation
        conversationRepository.save(conversation);
        return PromptMapping.mapToResponse(iaResponse);
    }

    /**
     * Determines which personality to use for a given prompt.
     * 
     * @param promptDTO The prompt data containing an optional personality ID
     * @param conversation The current conversation
     * @return The personality to use for the AI response
     * @throws ResourceNotFoundException if the specified personality doesn't exist or no personalities are available
     */
    private Personality getPersonalityForPrompt(PromptDTO promptDTO, Conversation conversation) {
        // If a specific personality ID is provided in the prompt, use that
        if (promptDTO.getPersonalityId() != null) {
            return personalityCache.getPersonalityById(promptDTO.getPersonalityId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "La personalidad especificada no existe. Por favor, verifica el ID proporcionado."));
        }

        // If there are previous responses in the conversation, use the same personality as the last response
        if (!conversation.getAiResponses().isEmpty()) {
            AIResponse lastResponse = conversation.getAiResponses().get(conversation.getAiResponses().size() - 1);
            if (lastResponse.getPersonality() != null) {
                return lastResponse.getPersonality();
            }
        }

        // Default to the first available personality
        return personalityCache.getFirstPersonality()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró ninguna personalidad. Por favor, crea al menos una personalidad."));
    }

    /**
     * Finds a user by their ID.
     * 
     * @param userId The ID of the user to find
     * @return The user entity
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    /**
     * Gets an existing conversation for a user or creates a new one if none exists.
     * 
     * @param user The user whose conversation to get or create
     * @return The conversation entity with all messages and responses loaded
     */
    private Conversation getOrCreateConversation(User user) {
        Conversation conversation = user.getConversation();
        if (conversation == null) {
            // Create a new conversation if the user doesn't have one
            conversation = initializeNewConversation(user);
        } else {
            // Load the full conversation with all messages and responses
            conversation = conversationRepository.findWithMessagesAndResponses(conversation.getId())
                    .orElse(conversation);
        }
        return conversation;
    }

    /**
     * Initializes a new conversation for a user.
     * 
     * @param user The user for whom to create a conversation
     * @return The newly created conversation entity
     */
    private Conversation initializeNewConversation(User user) {
        // Create a new conversation with a default name based on the user's email
        Conversation conversation = new Conversation(String.format(DEFAULT_CONVERSATION_NAME_TEMPLATE, user.getEmail()));
        conversation.setUser(user);

        return conversationRepository.save(conversation);
    }

    /**
     * Adds a user message to a conversation.
     * 
     * @param conversation The conversation to add the message to
     * @param content The content of the user message
     * @return The newly created user message entity
     */
    private UserMessage addUserMessageToConversation(Conversation conversation, String content) {
        // Create and save the user message
        UserMessage newUserMessage = new UserMessage(content);
        newUserMessage.setConversation(conversation);
        conversation.getUserMessages().add(newUserMessage);
        return userMessageRepository.save(newUserMessage);
    }

    /**
     * Adds an AI response to a conversation.
     * 
     * @param conversation The conversation to add the response to
     * @param content The content of the AI response
     * @param userMessage The user message that this response is replying to
     * @param personality The personality used to generate this response
     * @return The newly created AI response entity
     */
    private AIResponse addAssistantMessageToConversation(Conversation conversation, String content, UserMessage userMessage, Personality personality) {
        // Create and save the AI response
        AIResponse aiResponse = new AIResponse(content);
        aiResponse.setConversation(conversation);
        aiResponse.setUserMessage(userMessage);
        aiResponse.setPersonality(personality);
        conversation.getAiResponses().add(aiResponse);
        return aiResponseRepository.save(aiResponse);
    }

    /**
     * Processes a prompt with conversation history and the selected personality.
     * 
     * @param conversation The current conversation containing message history
     * @param personality The personality to use for the AI response
     * @return The response from the AI model
     */
    private OllamaResponse processPromptWithHistory(Conversation conversation, Personality personality) {
        // Get the base prompt from the personality
        String basePrompt = personality.getBasePrompt();
        // Build a prompt with the conversation history using a sliding window
        String historyPrompt = buildPromptWithWindow(conversation, 8);
        // Combine the base prompt and history prompt
        String fullPrompt = basePrompt + "\n\n" + historyPrompt;

        // Create a DTO for the Ollama service and send the prompt
        OllamaDTO ollamaDTO = OllamaMapping.toOllamaDTO(new PromptDTO(fullPrompt), iaModel);
        return iaService.sendPrompt(ollamaDTO);
    }

    /**
     * Builds a prompt string containing the most recent conversation history.
     * Uses a sliding window approach to limit the context size.
     * 
     * @param conversation The conversation containing the message history
     * @param windowSize The maximum number of message pairs to include
     * @return A formatted string containing the conversation history
     */
    private String buildPromptWithWindow(Conversation conversation, int windowSize) {
        StringBuilder sb = new StringBuilder();
        List<UserMessage> userMessages = conversation.getUserMessages();

        // Calculate the starting index based on the window size
        int startIndex = Math.max(0, userMessages.size() - windowSize);

        // Iterate through the messages within the window
        for (int i = startIndex; i < userMessages.size(); i++) {
            UserMessage userMessage = userMessages.get(i);
            // Add the user message
            sb.append(USER_ROLE).append(": ").append(userMessage.getContent()).append("\n");

            // Find and add the corresponding AI response if it exists
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

    /**
     * Clears all messages from a user's conversation.
     * 
     * @param userId The ID of the user whose conversation should be cleared
     */
    @Transactional
    public void clearConversation(String userId) {
        // Find the user by ID
        User user = findUserById(Long.parseLong(userId));
        Conversation conversation = user.getConversation();

        // If the user has an active conversation, clear all messages
        if (conversation != null) {
            conversation.getUserMessages().clear();
            conversation.getAiResponses().clear();

            // Save the empty conversation
            conversationRepository.save(conversation);
        }
    }
}
