package com.blinky.apillama3blinky.service;

import com.blinky.apillama3blinky.cache.PersonalityCache;
import com.blinky.apillama3blinky.controller.dto.PromptDTO;
import com.blinky.apillama3blinky.controller.response.OllamaResponse;
import com.blinky.apillama3blinky.model.*;
import com.blinky.apillama3blinky.repository.AIResponseRepository;
import com.blinky.apillama3blinky.repository.ConversationRepository;
import com.blinky.apillama3blinky.repository.UserMessageRepository;
import com.blinky.apillama3blinky.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LlamaApiServiceTest {

    @Mock
    private OllamaService ollamaService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private UserMessageRepository userMessageRepository;

    @Mock
    private AIResponseRepository aiResponseRepository;

    @Mock
    private PersonalityService personalityService;

    @Mock
    private PersonalityCache personalityCache;

    private LlamaApiService llamaApiService;

    @BeforeEach
    void setUp() {
        llamaApiService = new LlamaApiService(
                ollamaService,
                userRepository,
                conversationRepository,
                userMessageRepository,
                aiResponseRepository,
                personalityService,
                personalityCache
        );
        ReflectionTestUtils.setField(llamaApiService, "iaModel", "llama3");
    }

    /**
     * Test that the sliding window only includes the last N interactions.
     */
    @Test
    void testBuildPromptWithWindow_ShouldLimitToWindowSize() throws Exception {
        // Create a conversation with more messages than the window size
        Conversation conversation = new Conversation("Test Conversation");
        List<UserMessage> userMessages = new ArrayList<>();
        List<AIResponse> aiResponses = new ArrayList<>();

        // Create 10 user messages and AI responses
        for (int i = 0; i < 10; i++) {
            UserMessage userMessage = new UserMessage("User message " + i);
            // Set ID using reflection since there's no setId method
            ReflectionTestUtils.setField(userMessage, "id", (long) i);
            userMessages.add(userMessage);

            AIResponse aiResponse = new AIResponse("AI response " + i);
            aiResponse.setUserMessage(userMessage);
            aiResponses.add(aiResponse);
        }

        conversation.setUserMessages(userMessages);
        conversation.setAiResponses(aiResponses);

        // Use reflection to access the private method
        Method buildPromptWithWindowMethod = LlamaApiService.class.getDeclaredMethod(
                "buildPromptWithWindow", Conversation.class, int.class);
        buildPromptWithWindowMethod.setAccessible(true);

        // Test with window size 5
        String result = (String) buildPromptWithWindowMethod.invoke(llamaApiService, conversation, 5);

        // Verify that only the last 5 messages are included
        assertFalse(result.contains("User message 0"));
        assertFalse(result.contains("AI response 0"));
        assertFalse(result.contains("User message 4"));
        assertFalse(result.contains("AI response 4"));
        assertTrue(result.contains("User message 5"));
        assertTrue(result.contains("AI response 5"));
        assertTrue(result.contains("User message 9"));
        assertTrue(result.contains("AI response 9"));
    }

    /**
     * Test that the fetch join method is used to avoid N+1 queries.
     */
    @Test
    void testGetOrCreateConversation_ShouldUseFetchJoin() throws Exception {
        // Create a user with a conversation
        User user = new User();
        user.setId(1L);
        Conversation conversation = new Conversation("Test Conversation");
        // Set ID using reflection since there's no setId method
        ReflectionTestUtils.setField(conversation, "id", 1L);
        user.setConversation(conversation);

        // Mock the repository to return the conversation with fetch join
        when(conversationRepository.findWithMessagesAndResponses(anyLong()))
                .thenReturn(Optional.of(conversation));

        // Use reflection to access the private method
        Method getOrCreateConversationMethod = LlamaApiService.class.getDeclaredMethod(
                "getOrCreateConversation", User.class);
        getOrCreateConversationMethod.setAccessible(true);

        // Call the method
        getOrCreateConversationMethod.invoke(llamaApiService, user);

        // Verify that the fetch join method was called
        verify(conversationRepository).findWithMessagesAndResponses(conversation.getId());
    }

    /**
     * Test that the personality cache is used instead of querying the database.
     */
    @Test
    void testGetPersonalityForPrompt_ShouldUseCache() throws Exception {
        // Create a prompt with a personality ID
        PromptDTO promptDTO = new PromptDTO("Test prompt");
        promptDTO.setPersonalityId(1L);

        // Create a conversation
        Conversation conversation = new Conversation("Test Conversation");

        // Create a personality
        Personality personality = new Personality("Test Personality", "Base prompt", "Description");
        // Set ID using reflection since there's no setId method
        ReflectionTestUtils.setField(personality, "id", 1L);

        // Mock the cache to return the personality
        when(personalityCache.getPersonalityById(1L)).thenReturn(Optional.of(personality));

        // Use reflection to access the private method
        Method getPersonalityForPromptMethod = LlamaApiService.class.getDeclaredMethod(
                "getPersonalityForPrompt", PromptDTO.class, Conversation.class);
        getPersonalityForPromptMethod.setAccessible(true);

        // Call the method
        Personality result = (Personality) getPersonalityForPromptMethod.invoke(
                llamaApiService, promptDTO, conversation);

        // Verify that the cache was used and the service was not called
        verify(personalityCache).getPersonalityById(1L);
        verify(personalityService, never()).getPersonalityById(anyLong());
        assertEquals(personality, result);
    }
}
