package com.blinky.apillama3blinky;

import com.blinky.apillama3blinky.controller.dto.UserDTO;
import com.blinky.apillama3blinky.controller.dto.UserUpdateDTO;
import com.blinky.apillama3blinky.controller.response.UserResponseDTO;
import com.blinky.apillama3blinky.model.User;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for the UserController.
 * 
 * This test class provides complete test coverage for all UserController endpoints:
 * - GET /api/user (list all users)
 * - GET /api/user/{id} (get a specific user)
 * - GET /api/user/email/{email} (get a user by email)
 * - POST /api/user (create a user)
 * - PUT /api/user/{id} (update a user)
 * - DELETE /api/user/{id} (delete a user)
 * 
 * Testing strategy:
 * 1. Tests both positive and negative scenarios for each endpoint
 * 2. Tests authentication requirements (authenticated vs unauthenticated)
 * 3. Tests validation rules (e.g., invalid data, non-existent resources)
 * 
 * Test setup:
 * - Uses H2 in-memory database for testing
 * - Creates test users: regular user and admin user
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
public class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String baseUrl;
    private User regularUser;
    private User adminUser;
    private String regularUserToken;
    private String adminUserToken;
    private HttpHeaders regularUserHeaders;
    private HttpHeaders adminUserHeaders;
    private List<User> testUsers;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/api/user";

        // Clean up any existing users
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

        // Create additional test users
        testUsers = createTestUsers(3);
    }

    @AfterEach
    public void tearDown() {
        // Clean up all test data
        userRepository.deleteAll();
    }

    // ========== GET /api/user (Get All Users) Tests ==========

    @Test
    public void testGetAllUsers_AsAuthenticatedUser_ShouldSucceed() {
        // Send request as regular user
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<List<UserResponseDTO>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<UserResponseDTO>>() {}
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<UserResponseDTO> users = response.getBody();
        assertNotNull(users);
        // Should include regularUser, adminUser, and the 3 test users
        assertEquals(5, users.size());
    }

    @Test
    public void testGetAllUsers_WithoutAuth_ShouldReturnForbidden() {
        // Send request without auth token
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class);

        // Verify response
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // ========== GET /api/user/{id} (Get User by ID) Tests ==========

    @Test
    public void testGetUserById_AsAuthenticatedUser_ShouldSucceed() {
        // Get a test user
        User user = testUsers.get(0);

        // Send request as regular user
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<UserResponseDTO> response = restTemplate.exchange(
                baseUrl + "/" + user.getId(),
                HttpMethod.GET,
                request,
                UserResponseDTO.class
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserResponseDTO userDTO = response.getBody();
        assertNotNull(userDTO);
        assertEquals(user.getId(), userDTO.getId());
        assertEquals(user.getEmail(), userDTO.getEmail());
        assertEquals(user.isAdmin(), userDTO.isAdmin());
        assertEquals(user.getUsername(), userDTO.getUsername());
    }

    @Test
    public void testGetUserById_NonExistentId_ShouldReturnNotFound() {
        // Use a non-existent user ID
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

    // ========== GET /api/user/email/{email} (Get User by Email) Tests ==========

    @Test
    public void testGetUserByEmail_AsAuthenticatedUser_ShouldSucceed() {
        // Get a test user
        User user = testUsers.get(0);

        // Send request as regular user
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<UserResponseDTO> response = restTemplate.exchange(
                baseUrl + "/email/" + user.getEmail(),
                HttpMethod.GET,
                request,
                UserResponseDTO.class
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserResponseDTO userDTO = response.getBody();
        assertNotNull(userDTO);
        assertEquals(user.getId(), userDTO.getId());
        assertEquals(user.getEmail(), userDTO.getEmail());
        assertEquals(user.isAdmin(), userDTO.isAdmin());
        assertEquals(user.getUsername(), userDTO.getUsername());
    }

    @Test
    public void testGetUserByEmail_NonExistentEmail_ShouldReturnNotFound() {
        // Use a non-existent email
        String nonExistentEmail = "nonexistent@example.com";

        // Send request as regular user
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/email/" + nonExistentEmail,
                HttpMethod.GET,
                request,
                String.class
        );

        // Verify response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ========== POST /api/user (Create User) Tests ==========

    @Test
    public void testCreateUser_AsAuthenticatedUser_ShouldSucceed() {
        // Create user DTO
        String uniqueEmail = "new" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        UserDTO userDTO = new UserDTO(
                null,
                uniqueEmail,
                "password123",
                false,
                "New Test User"
        );

        // Send request as regular user
        HttpEntity<UserDTO> request = new HttpEntity<>(userDTO, regularUserHeaders);
        ResponseEntity<UserResponseDTO> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                UserResponseDTO.class
        );

        // Verify response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        UserResponseDTO createdUser = response.getBody();
        assertNotNull(createdUser);
        assertEquals(uniqueEmail, createdUser.getEmail());
        assertEquals("New Test User", createdUser.getUsername());
        assertFalse(createdUser.isAdmin());

        // Verify user was saved to database
        assertTrue(userRepository.existsById(createdUser.getId()));
    }

    @Test
    public void testCreateUser_WithInvalidData_ShouldReturnBadRequest() {
        // Create user DTO with invalid data (invalid email)
        UserDTO userDTO = new UserDTO(
                null,
                "invalid-email", // Invalid: not a valid email
                "password123",
                false,
                "Invalid User"
        );

        // Send request as regular user
        HttpEntity<UserDTO> request = new HttpEntity<>(userDTO, regularUserHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== PUT /api/user/{id} (Update User) Tests ==========

    @Test
    public void testUpdateUser_AsAuthenticatedUser_ShouldSucceed() {
        // Get a test user
        User user = testUsers.get(0);

        // Create update DTO
        UserUpdateDTO updateDTO = new UserUpdateDTO(
                "updated" + UUID.randomUUID().toString().substring(0, 8) + "@example.com",
                "updatedPassword123",
                false,
                "Updated Username"
        );

        // Send request as regular user
        HttpEntity<UserUpdateDTO> request = new HttpEntity<>(updateDTO, regularUserHeaders);
        ResponseEntity<UserResponseDTO> response = restTemplate.exchange(
                baseUrl + "/" + user.getId(),
                HttpMethod.PUT,
                request,
                UserResponseDTO.class
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserResponseDTO updatedUser = response.getBody();
        assertNotNull(updatedUser);
        assertEquals(user.getId(), updatedUser.getId());
        assertEquals(updateDTO.getEmail(), updatedUser.getEmail());
        assertEquals(updateDTO.getUsername(), updatedUser.getUsername());
        assertEquals(updateDTO.isAdmin(), updatedUser.isAdmin());
    }

    @Test
    public void testUpdateUser_NonExistentId_ShouldReturnError() {
        // Use a non-existent user ID
        Long nonExistentId = 999999L;

        // Create update DTO
        UserUpdateDTO updateDTO = new UserUpdateDTO(
                "updated@example.com",
                "updatedPassword123",
                false,
                "Updated Username"
        );

        // Send request as regular user
        HttpEntity<UserUpdateDTO> request = new HttpEntity<>(updateDTO, regularUserHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + nonExistentId,
                HttpMethod.PUT,
                request,
                String.class
        );

        // Verify response
        // The current implementation throws an exception when trying to update a non-existent user
        // This results in a 500 Internal Server Error
        assertTrue(response.getStatusCode().is5xxServerError());
    }

    // ========== DELETE /api/user/{id} (Delete User) Tests ==========

    @Test
    public void testDeleteUser_AsAuthenticatedUser_ShouldSucceed() {
        // Get a test user
        User user = testUsers.get(0);

        // Send request as regular user
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + user.getId(),
                HttpMethod.DELETE,
                request,
                Void.class
        );

        // Verify response
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify user is deleted
        assertFalse(userRepository.existsById(user.getId()));
    }

    @Test
    public void testDeleteUser_NonExistentId_ShouldReturnNotFound() {
        // Use a non-existent user ID
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

    private List<User> createTestUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String uniqueEmail = "test" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            User user = new User();
            user.setEmail(uniqueEmail);
            user.setPassword("password");
            user.setUsername("Test User " + (i + 1));
            user.setAdmin(false);
            users.add(userRepository.save(user));
        }
        return users;
    }
}
