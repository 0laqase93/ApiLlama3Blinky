package com.blinky.apillama3blinky;

import com.blinky.apillama3blinky.controller.dto.PersonalityDTO;
import com.blinky.apillama3blinky.controller.response.PersonalityResponseDTO;
import com.blinky.apillama3blinky.model.Personality;
import com.blinky.apillama3blinky.model.User;
import com.blinky.apillama3blinky.repository.PersonalityRepository;
import com.blinky.apillama3blinky.repository.UserRepository;
import com.blinky.apillama3blinky.security.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for the PersonalityController.
 * 
 * This test class provides complete test coverage for all PersonalityController endpoints:
 * - GET /api/personalities (list all personalities)
 * - GET /api/personalities/{id} (get a specific personality)
 * - POST /api/personalities (create a personality)
 * - PUT /api/personalities/{id} (update a personality)
 * - DELETE /api/personalities/{id} (delete a personality)
 * 
 * Testing strategy:
 * 1. Tests both positive and negative scenarios for each endpoint
 * 2. Tests authentication requirements (authenticated vs unauthenticated)
 * 3. Tests validation rules (e.g., invalid data, non-existent resources)
 * 
 * Test setup:
 * - Uses H2 in-memory database for testing
 * - Creates test users: regular user and admin user
 * - Creates test personalities
 * - Generates proper JWT tokens with appropriate roles and user IDs
 * - Cleans up all test data after each test
 * 
 * Each test method follows a consistent pattern:
 * 1. Arrange: Set up test data and request parameters
 * 2. Act: Send the HTTP request to the endpoint
 * 3. Assert: Verify the response status, body, and any side effects
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PersonalityControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonalityRepository personalityRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String baseUrl;
    private User regularUser;
    private User adminUser;
    private String regularUserToken;
    private String adminUserToken;
    private HttpHeaders regularUserHeaders;
    private HttpHeaders adminUserHeaders;
    private List<Personality> testPersonalities;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/api/personalities";

        // Clean up any existing personalities and users
        personalityRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        regularUser = createUser("regular@example.com", "Regular User", false);
        adminUser = createUser("admin@example.com", "Admin User", true);

        // Generate JWT tokens
        regularUserToken = generateToken(regularUser, false);
        adminUserToken = generateToken(adminUser, true);

        // Set up headers
        regularUserHeaders = createHeaders(regularUserToken);
        adminUserHeaders = createHeaders(adminUserToken);

        // Create test personalities
        testPersonalities = createTestPersonalities(3);
    }

    @AfterEach
    public void tearDown() {
        // Clean up all test data
        personalityRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========== GET /api/personalities (Get All Personalities) Tests ==========

    @Test
    public void testGetAllPersonalities_AsAuthenticatedUser_ShouldSucceed() {
        // Send request as regular user
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<List<PersonalityResponseDTO>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<PersonalityResponseDTO>>() {}
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<PersonalityResponseDTO> personalities = response.getBody();
        assertNotNull(personalities);
        assertEquals(testPersonalities.size(), personalities.size());
    }

    @Test
    public void testGetAllPersonalities_WithoutAuth_ShouldReturnForbidden() {
        // Send request without auth token
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class);

        // Verify response
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // ========== GET /api/personalities/{id} (Get Personality by ID) Tests ==========

    @Test
    public void testGetPersonalityById_AsAuthenticatedUser_ShouldSucceed() {
        // Get a test personality
        Personality personality = testPersonalities.get(0);

        // Send request as regular user
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<PersonalityResponseDTO> response = restTemplate.exchange(
                baseUrl + "/" + personality.getId(),
                HttpMethod.GET,
                request,
                PersonalityResponseDTO.class
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        PersonalityResponseDTO personalityDTO = response.getBody();
        assertNotNull(personalityDTO);
        assertEquals(personality.getId(), personalityDTO.getId());
        assertEquals(personality.getName(), personalityDTO.getName());
        assertEquals(personality.getBasePrompt(), personalityDTO.getBasePrompt());
        assertEquals(personality.getDescription(), personalityDTO.getDescription());
    }

    @Test
    public void testGetPersonalityById_NonExistentId_ShouldReturnNotFound() {
        // Use a non-existent personality ID
        Long nonExistentId = 999999L;

        // Send request as regular user
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + nonExistentId,
                HttpMethod.GET,
                request,
                String.class
        );

        // Verify response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ========== POST /api/personalities (Create Personality) Tests ==========

    @Test
    public void testCreatePersonality_AsAuthenticatedUser_ShouldSucceed() {
        // Create personality DTO
        PersonalityDTO personalityDTO = new PersonalityDTO(
                "New Test Personality",
                "This is a base prompt for testing",
                "This is a test description"
        );

        // Send request as regular user
        HttpEntity<PersonalityDTO> request = new HttpEntity<>(personalityDTO, regularUserHeaders);
        ResponseEntity<PersonalityResponseDTO> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                PersonalityResponseDTO.class
        );

        // Verify response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        PersonalityResponseDTO createdPersonality = response.getBody();
        assertNotNull(createdPersonality);
        assertEquals("New Test Personality", createdPersonality.getName());
        assertEquals("This is a base prompt for testing", createdPersonality.getBasePrompt());
        assertEquals("This is a test description", createdPersonality.getDescription());

        // Verify personality was saved to database
        assertTrue(personalityRepository.existsById(createdPersonality.getId()));
    }

    @Test
    public void testCreatePersonality_WithInvalidData_ShouldReturnBadRequest() {
        // Create personality DTO with invalid data (null name)
        PersonalityDTO personalityDTO = new PersonalityDTO(
                null, // Invalid: name is null
                "This is a base prompt for testing",
                "This is a test description"
        );

        // Send request as regular user
        HttpEntity<PersonalityDTO> request = new HttpEntity<>(personalityDTO, regularUserHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== PUT /api/personalities/{id} (Update Personality) Tests ==========

    @Test
    public void testUpdatePersonality_AsAuthenticatedUser_ShouldSucceed() {
        // Get a test personality
        Personality personality = testPersonalities.get(0);

        // Create update DTO
        PersonalityDTO updateDTO = new PersonalityDTO(
                "Updated Personality Name",
                "Updated base prompt",
                "Updated description"
        );

        // Send request as regular user
        HttpEntity<PersonalityDTO> request = new HttpEntity<>(updateDTO, regularUserHeaders);
        ResponseEntity<PersonalityResponseDTO> response = restTemplate.exchange(
                baseUrl + "/" + personality.getId(),
                HttpMethod.PUT,
                request,
                PersonalityResponseDTO.class
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        PersonalityResponseDTO updatedPersonality = response.getBody();
        assertNotNull(updatedPersonality);
        assertEquals(personality.getId(), updatedPersonality.getId());
        assertEquals("Updated Personality Name", updatedPersonality.getName());
        assertEquals("Updated base prompt", updatedPersonality.getBasePrompt());
        assertEquals("Updated description", updatedPersonality.getDescription());
    }

    @Test
    public void testUpdatePersonality_NonExistentId_ShouldReturnNotFound() {
        // Use a non-existent personality ID
        Long nonExistentId = 999999L;

        // Create update DTO
        PersonalityDTO updateDTO = new PersonalityDTO(
                "Updated Personality Name",
                "Updated base prompt",
                "Updated description"
        );

        // Send request as regular user
        HttpEntity<PersonalityDTO> request = new HttpEntity<>(updateDTO, regularUserHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + nonExistentId,
                HttpMethod.PUT,
                request,
                String.class
        );

        // Verify response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ========== DELETE /api/personalities/{id} (Delete Personality) Tests ==========

    @Test
    public void testDeletePersonality_AsAuthenticatedUser_ShouldSucceed() {
        // Get a test personality
        Personality personality = testPersonalities.get(0);

        // Send request as regular user
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + personality.getId(),
                HttpMethod.DELETE,
                request,
                Void.class
        );

        // Verify response
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify personality is deleted
        assertFalse(personalityRepository.existsById(personality.getId()));
    }

    @Test
    public void testDeletePersonality_NonExistentId_ShouldReturnNotFound() {
        // Use a non-existent personality ID
        Long nonExistentId = 999999L;

        // Send request as regular user
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + nonExistentId,
                HttpMethod.DELETE,
                request,
                String.class
        );

        // Verify response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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

    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private List<Personality> createTestPersonalities(int count) {
        List<Personality> personalities = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Personality personality = new Personality(
                    "Test Personality " + (i + 1),
                    "Base prompt for test personality " + (i + 1),
                    "Description for test personality " + (i + 1)
            );
            personalities.add(personalityRepository.save(personality));
        }
        return personalities;
    }
}