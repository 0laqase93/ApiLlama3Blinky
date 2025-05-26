package com.blinky.apillama3blinky.controller;

import com.blinky.apillama3blinky.controller.dto.LoginDTO;
import com.blinky.apillama3blinky.controller.dto.RegisterDTO;
import com.blinky.apillama3blinky.controller.dto.ResetPasswordDTO;
import com.blinky.apillama3blinky.controller.dto.VerifyPasswordDTO;
import com.blinky.apillama3blinky.controller.response.AuthResponse;
import com.blinky.apillama3blinky.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication-related API endpoints.
 * Handles user registration, login, password management, and service availability checks.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user in the system.
     * 
     * @param registerDTO Data transfer object containing registration information
     * @return Response entity with authentication details and HTTP 201 Created status
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return new ResponseEntity<>(authService.registerUserFromDTO(registerDTO), HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and provides a JWT token.
     * 
     * @param loginDTO Data transfer object containing login credentials
     * @return Response entity with authentication details and token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(authService.loginUser(loginDTO));
    }

    /**
     * Resets a user's password.
     * 
     * @param resetPasswordDTO Data transfer object containing password reset information
     * @param request The HTTP request containing the JWT token for user authentication
     * @return Response entity indicating success or failure
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Boolean> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO, HttpServletRequest request) {
        return authService.resetPasswordWithResponse(resetPasswordDTO, request);
    }

    /**
     * Verifies if a provided password matches the user's current password.
     * 
     * @param verifyPasswordDTO Data transfer object containing the password to verify
     * @param request The HTTP request containing the JWT token for user authentication
     * @return Response entity indicating if the password is valid
     */
    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifyPassword(@Valid @RequestBody VerifyPasswordDTO verifyPasswordDTO, HttpServletRequest request) {
        return authService.verifyPasswordWithResponse(verifyPasswordDTO, request);
    }

    /**
     * Simple endpoint to check if the authentication service is available.
     * 
     * @return Response entity with a status message
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return authService.ping();
    }
}
