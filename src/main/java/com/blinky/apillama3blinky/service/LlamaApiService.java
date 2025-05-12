package com.blinky.apillama3blinky.service;

import com.blinky.apillama3blinky.config.PromptConfig;
import com.blinky.apillama3blinky.controller.dto.OllamaDTO;
import com.blinky.apillama3blinky.controller.dto.PromptDTO;
import com.blinky.apillama3blinky.controller.response.OllamaResponse;
import com.blinky.apillama3blinky.controller.response.PromptResponse;
import com.blinky.apillama3blinky.mapping.OllamaMapping;
import com.blinky.apillama3blinky.mapping.PromptMapping;
import com.blinky.apillama3blinky.model.AIResponse;
import com.blinky.apillama3blinky.model.Conversation;
import com.blinky.apillama3blinky.model.Message;
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

    public LlamaApiService(OllamaService iaService,
                           PromptService promptService,
                           UserRepository userRepository,
                           ConversationRepository conversationRepository,
                           UserMessageRepository userMessageRepository,
                           AIResponseRepository aiResponseRepository) {
        this.iaService = iaService;
        this.promptService = promptService;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.userMessageRepository = userMessageRepository;
        this.aiResponseRepository = aiResponseRepository;
    }

    @Transactional
    public PromptResponse sendPrompt(PromptDTO promptDTO) {
        User user = findUserById(promptDTO.getUserId());
        Conversation conversation = getOrCreateConversation(user);

        UserMessage userMessage = addUserMessageToConversation(conversation, promptDTO.getPrompt());
        OllamaResponse iaResponse = processPromptWithHistory(conversation);
        addAssistantMessageToConversation(conversation, iaResponse.getResponse(), userMessage);

        conversationRepository.save(conversation);
        return PromptMapping.mapToResponse(iaResponse);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
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

        PromptConfig initialConfig = promptService.getPromptFor(STARTER_PROMPT_NAME);

        // Keep backward compatibility with the old model
        Message systemMessage = new Message(SYSTEM_ROLE, initialConfig.getPrompt());
        systemMessage.setConversation(conversation);
        conversation.getMessages().add(systemMessage);

        // Note: In the new model, we don't have a concept of system messages yet
        // This could be added in the future if needed

        return conversationRepository.save(conversation);
    }

    private UserMessage addUserMessageToConversation(Conversation conversation, String content) {
        // Keep backward compatibility with the old model
        Message userMessage = new Message(USER_ROLE, content);
        userMessage.setConversation(conversation);
        conversation.getMessages().add(userMessage);

        // Use the new model
        UserMessage newUserMessage = new UserMessage(content);
        newUserMessage.setConversation(conversation);
        conversation.getUserMessages().add(newUserMessage);
        return userMessageRepository.save(newUserMessage);
    }

    private AIResponse addAssistantMessageToConversation(Conversation conversation, String content, UserMessage userMessage) {
        // Keep backward compatibility with the old model
        Message assistantMessage = new Message(ASSISTANT_ROLE, content);
        assistantMessage.setConversation(conversation);
        conversation.getMessages().add(assistantMessage);

        // Use the new model
        AIResponse aiResponse = new AIResponse(content);
        aiResponse.setConversation(conversation);
        aiResponse.setUserMessage(userMessage);
        conversation.getAiResponses().add(aiResponse);
        return aiResponseRepository.save(aiResponse);
    }

    private OllamaResponse processPromptWithHistory(Conversation conversation) {
        String fullPrompt = buildPromptWithHistory(conversation.getMessages());
        OllamaDTO ollamaDTO = OllamaMapping.toOllamaDTO(new PromptDTO(fullPrompt), iaModel);
        return iaService.sendPrompt(ollamaDTO);
    }

    private String buildPromptWithHistory(List<Message> messages) {
        // For now, we continue using the old model for building the prompt history
        // This could be updated in the future to use the new model (userMessages and aiResponses)
        return messages.stream()
                .map(message -> message.getRole() + ": " + message.getContent())
                .reduce(new StringBuilder(),
                        (sb, message) -> sb.append(message).append("\n"),
                        StringBuilder::append)
                .toString();
    }

    // This method can be used in the future to build the prompt history from the new model
    private String buildPromptWithNewModel(Conversation conversation) {
        StringBuilder sb = new StringBuilder();

        // Add system messages from the old model (these don't exist in the new model yet)
        conversation.getMessages().stream()
                .filter(message -> SYSTEM_ROLE.equals(message.getRole()))
                .forEach(message -> sb.append(SYSTEM_ROLE).append(": ").append(message.getContent()).append("\n"));

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
            // Clear old model messages
            conversation.getMessages().clear();

            // Clear new model messages
            conversation.getUserMessages().clear();
            conversation.getAiResponses().clear();

            conversationRepository.save(conversation);
        }
    }
}
