package com.blinky.apillama3blinky;

import com.blinky.apillama3blinky.controller.dto.PromptDTO;
import com.blinky.apillama3blinky.controller.response.PromptResponse;
import com.blinky.apillama3blinky.model.Personality;
import com.blinky.apillama3blinky.model.User;
import com.blinky.apillama3blinky.repository.PersonalityRepository;
import com.blinky.apillama3blinky.repository.UserRepository;
import com.blinky.apillama3blinky.security.JwtUtil;
import com.blinky.apillama3blinky.service.LlamaApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprehensive integration tests for the LlamaApiController.
 * 
 * This test class provides complete test coverage for all LlamaApiController endpoints:
 * - POST /api/llama/send_prompt (send a prompt to the LLM)
 * 
 * Testing strategy:
 * 1. Tests both positive and negative scenarios for each endpoint
 * 2. Tests authentication requirements (authenticated vs unauthenticated)
 * 3. Tests validation rules (e.g., invalid data)
 * 
 * Test setup:
 * - Uses H2 in-memory database for testing
 * - Creates test users and personalities
 * - Mocks the LlamaApiService to avoid making actual API calls
 * - Generates proper JWT tokens with appropriate roles and user IDs
 * - Cleans up all test data after each test
 * 
 * Each test method follows a consistent pattern:
 * 1. Arrange: Set up test data and request parameters
 * 2. Act: Send the HTTP request to the endpoint
 * 3. Assert: Verify the response status, body, and any side effects
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LlamaApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonalityRepository personalityRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LlamaApiService llamaApiService;

    private User regularUser;
    private User adminUser;
    private String regularUserToken;
    private String adminUserToken;
    private Personality testPersonality;

    @BeforeEach
    public void setUp() {
        // Clean up any existing users and personalities
        userRepository.deleteAll();
        personalityRepository.deleteAll();

        // Create test users
        regularUser = createUser("regular@example.com", "Regular User", false);
        adminUser = createUser("admin@example.com", "Admin User", true);

        // Generate JWT tokens
        regularUserToken = generateToken(regularUser, false);
        adminUserToken = generateToken(adminUser, true);

        // Create a test personality
        testPersonality = createPersonality("Test Personality", "This is a base prompt for testing", "This is a test description");

        // Mock the LlamaApiService
        PromptResponse mockResponse = new PromptResponse("This is a mock response from the LLM");
        when(llamaApiService.sendPrompt(any(PromptDTO.class))).thenReturn(mockResponse);
    }

    @AfterEach
    public void tearDown() {
        // Clean up all test data
        userRepository.deleteAll();
        personalityRepository.deleteAll();
    }

    // ========== POST /api/llama/send_prompt (Send Prompt) Tests ==========

    @Test
    public void testSendPrompt_AsAuthenticatedUser_ShouldSucceed() throws Exception {
        // Create prompt DTO
        PromptDTO promptDTO = new PromptDTO("This is a test prompt");
        promptDTO.setPersonalityId(testPersonality.getId());

        // Send request as regular user
        mockMvc.perform(MockMvcRequestBuilders.post("/api/llama/send_prompt")
                .header("Authorization", "Bearer " + regularUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(promptDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("This is a mock response from the LLM"));
    }

    @Test
    public void testSendPrompt_WithoutAuth_ShouldReturnForbidden() throws Exception {
        // Create prompt DTO
        PromptDTO promptDTO = new PromptDTO("This is a test prompt");
        promptDTO.setPersonalityId(testPersonality.getId());

        // Send request without auth token
        mockMvc.perform(MockMvcRequestBuilders.post("/api/llama/send_prompt")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(promptDTO)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testSendPrompt_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Create prompt DTO with invalid data (null prompt)
        PromptDTO promptDTO = new PromptDTO();
        promptDTO.setPersonalityId(testPersonality.getId());

        // Send request as regular user
        mockMvc.perform(MockMvcRequestBuilders.post("/api/llama/send_prompt")
                .header("Authorization", "Bearer " + regularUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(promptDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ========== Helper Methods ==========

    private User createUser(String email, String username, boolean isAdmin) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setUsername(username);
        user.setAdmin(isAdmin);
        return userRepository.save(user);
    }

    private String generateToken(User user, boolean isAdmin) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (isAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );

        return jwtUtil.generateToken(userDetails, user.isAdmin(), user.getId());
    }

    private Personality createPersonality(String name, String basePrompt, String description) {
        Personality personality = new Personality(name, basePrompt, description);
        return personalityRepository.save(personality);
    }
}