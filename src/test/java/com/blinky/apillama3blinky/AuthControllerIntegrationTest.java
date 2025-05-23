package com.blinky.apillama3blinky;

import com.blinky.apillama3blinky.controller.dto.LoginDTO;
import com.blinky.apillama3blinky.controller.dto.RegisterDTO;
import com.blinky.apillama3blinky.controller.dto.ResetPasswordDTO;
import com.blinky.apillama3blinky.controller.dto.VerifyPasswordDTO;
import com.blinky.apillama3blinky.controller.response.AuthResponse;
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
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for the AuthController.
 * 
 * This test class provides complete test coverage for all AuthController endpoints:
 * - POST /api/auth/register (register a new user)
 * - POST /api/auth/login (login a user)
 * - POST /api/auth/reset-password (reset a user's password)
 * - POST /api/auth/verify-password (verify a user's password)
 * - GET /api/auth/ping (simple ping endpoint)
 * 
 * Testing strategy:
 * 1. Tests both positive and negative scenarios for each endpoint
 * 2. Tests validation rules (e.g., invalid data, duplicate emails)
 * 3. Tests authentication and authorization
 * 
 * Test setup:
 * - Uses H2 in-memory database for testing
 * - Creates test users as needed
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
public class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String baseUrl;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/api/auth";

        // Clean up any existing users
        userRepository.deleteAll();
    }

    @AfterEach
    public void tearDown() {
        // Clean up all test data
        userRepository.deleteAll();
    }

    // ========== POST /api/auth/register Tests ==========

    @Test
    public void testRegister_WithValidData_ShouldSucceed() {
        // Create register DTO
        RegisterDTO registerDTO = new RegisterDTO(
                "newuser@example.com",
                "password123",
                "New User"
        );

        // Send request
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                baseUrl + "/register",
                registerDTO,
                AuthResponse.class
        );

        // Verify response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        AuthResponse authResponse = response.getBody();
        assertNotNull(authResponse);
        assertNotNull(authResponse.getToken());
        assertNotNull(authResponse.getUser());
        assertEquals("newuser@example.com", authResponse.getUser().getEmail());
        assertEquals("New User", authResponse.getUser().getUsername());
        assertFalse(authResponse.getUser().isAdmin());

        // Verify user was saved to database
        assertTrue(userRepository.findByEmail("newuser@example.com").isPresent());
    }

    @Test
    public void testRegister_WithExistingEmail_ShouldFail() {
        // Create a user first
        User existingUser = createUser("existing@example.com", "Existing User", false);

        // Try to register with the same email
        RegisterDTO registerDTO = new RegisterDTO(
                "existing@example.com",
                "password123",
                "Another User"
        );

        // Send request
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/register",
                registerDTO,
                String.class
        );

        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testRegister_WithInvalidData_ShouldFail() {
        // Create register DTO with invalid email
        RegisterDTO registerDTO = new RegisterDTO(
                "invalid-email", // Invalid email
                "password123",
                "Invalid User"
        );

        // Send request
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/register",
                registerDTO,
                String.class
        );

        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== POST /api/auth/login Tests ==========

    @Test
    public void testLogin_WithValidCredentials_ShouldSucceed() {
        // Create a user first
        User user = createUser("user@example.com", "Test User", false);

        // Create login DTO
        LoginDTO loginDTO = new LoginDTO(
                "user@example.com",
                "password" // This matches the password set in createUser method
        );

        // Send request
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                baseUrl + "/login",
                loginDTO,
                AuthResponse.class
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponse authResponse = response.getBody();
        assertNotNull(authResponse);
        assertNotNull(authResponse.getToken());
        assertNotNull(authResponse.getUser());
        assertEquals("user@example.com", authResponse.getUser().getEmail());
        assertEquals("Test User", authResponse.getUser().getUsername());
        assertFalse(authResponse.getUser().isAdmin());
    }

    @Test
    public void testLogin_WithInvalidCredentials_ShouldFail() {
        // Create a user first
        User user = createUser("user@example.com", "Test User", false);

        // Create login DTO with wrong password
        LoginDTO loginDTO = new LoginDTO(
                "user@example.com",
                "wrongpassword"
        );

        // Send request
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/login",
                loginDTO,
                String.class
        );

        // Verify response
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testLogin_WithNonExistentUser_ShouldFail() {
        // Create login DTO for non-existent user
        LoginDTO loginDTO = new LoginDTO(
                "nonexistent@example.com",
                "password"
        );

        // Send request
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/login",
                loginDTO,
                String.class
        );

        // Verify response
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ========== POST /api/auth/reset-password Tests ==========

    @Test
    public void testResetPassword_WithValidToken_ShouldSucceed() {
        // Create a user first
        User user = createUser("user@example.com", "Test User", false);

        // Generate token for the user
        String token = generateToken(user, false);

        // Create reset password DTO
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO("newpassword123");

        // Create headers with token
        HttpHeaders headers = createHeaders(token);

        // Send request
        HttpEntity<ResetPasswordDTO> request = new HttpEntity<>(resetPasswordDTO, headers);
        ResponseEntity<Boolean> response = restTemplate.exchange(
                baseUrl + "/reset-password",
                HttpMethod.POST,
                request,
                Boolean.class
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());

        // Verify password was updated by trying to login with new password
        LoginDTO loginDTO = new LoginDTO("user@example.com", "newpassword123");
        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/login",
                loginDTO,
                AuthResponse.class
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
    }

    @Test
    public void testResetPassword_WithoutToken_ShouldFail() {
        // Create reset password DTO
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO("newpassword123");

        // Send request without token
        ResponseEntity<Boolean> response = restTemplate.postForEntity(
                baseUrl + "/reset-password",
                resetPasswordDTO,
                Boolean.class
        );

        // Verify response
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ========== POST /api/auth/verify-password Tests ==========

    @Test
    public void testVerifyPassword_WithCorrectPassword_ShouldReturnTrue() {
        // Create a user first
        User user = createUser("user@example.com", "Test User", false);

        // Generate token for the user
        String token = generateToken(user, false);

        // Create verify password DTO with correct password
        VerifyPasswordDTO verifyPasswordDTO = new VerifyPasswordDTO("password");

        // Create headers with token
        HttpHeaders headers = createHeaders(token);

        // Send request
        HttpEntity<VerifyPasswordDTO> request = new HttpEntity<>(verifyPasswordDTO, headers);
        ResponseEntity<Boolean> response = restTemplate.exchange(
                baseUrl + "/verify-password",
                HttpMethod.POST,
                request,
                Boolean.class
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }

    @Test
    public void testVerifyPassword_WithIncorrectPassword_ShouldReturnFalse() {
        // Create a user first
        User user = createUser("user@example.com", "Test User", false);

        // Generate token for the user
        String token = generateToken(user, false);

        // Create verify password DTO with incorrect password
        VerifyPasswordDTO verifyPasswordDTO = new VerifyPasswordDTO("wrongpassword");

        // Create headers with token
        HttpHeaders headers = createHeaders(token);

        // Send request
        HttpEntity<VerifyPasswordDTO> request = new HttpEntity<>(verifyPasswordDTO, headers);
        ResponseEntity<Boolean> response = restTemplate.exchange(
                baseUrl + "/verify-password",
                HttpMethod.POST,
                request,
                Boolean.class
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
    }

    @Test
    public void testVerifyPassword_WithoutToken_ShouldFail() {
        // Create verify password DTO
        VerifyPasswordDTO verifyPasswordDTO = new VerifyPasswordDTO("password");

        // Send request without token
        ResponseEntity<Boolean> response = restTemplate.postForEntity(
                baseUrl + "/verify-password",
                verifyPasswordDTO,
                Boolean.class
        );

        // Verify response
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ========== GET /api/auth/ping Tests ==========

    @Test
    public void testPing_ShouldReturnPong() {
        // Send request
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/ping",
                String.class
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pong!", response.getBody());
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
}
