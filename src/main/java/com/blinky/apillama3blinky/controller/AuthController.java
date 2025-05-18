package com.blinky.apillama3blinky.controller;

import com.blinky.apillama3blinky.controller.dto.LoginDTO;
import com.blinky.apillama3blinky.controller.dto.RegisterDTO;
import com.blinky.apillama3blinky.controller.dto.ResetPasswordDTO;
import com.blinky.apillama3blinky.controller.dto.UserDTO;
import com.blinky.apillama3blinky.controller.dto.VerifyPasswordDTO;
import com.blinky.apillama3blinky.controller.response.AuthResponse;
import com.blinky.apillama3blinky.exception.UserNotFoundException;
import com.blinky.apillama3blinky.mapping.UserMapper;
import com.blinky.apillama3blinky.model.User;
import com.blinky.apillama3blinky.security.JwtUtil;
import com.blinky.apillama3blinky.service.AuthService;
import com.blinky.apillama3blinky.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, UserService userService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterDTO registerDTO) {
        AuthResponse authResponse = authService.registerUserFromDTO(registerDTO);
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginDTO loginDTO) {
        AuthResponse authResponse = authService.loginUser(loginDTO.getEmail(), loginDTO.getPassword());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Boolean> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO, HttpServletRequest request) {
        try {
            // Reset the password using the service
            authService.resetPasswordFromRequest(request, resetPasswordDTO.getNewPassword());

            // Return true if password was reset successfully
            return ResponseEntity.ok(true);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifyPassword(@Valid @RequestBody VerifyPasswordDTO verifyPasswordDTO, HttpServletRequest request) {
        try {
            // Verify the password using the service
            boolean isPasswordValid = authService.verifyPasswordFromRequest(request, verifyPasswordDTO.getPassword());

            // Return the result directly
            return ResponseEntity.ok(isPasswordValid);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}
