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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return new ResponseEntity<>(authService.registerUserFromDTO(registerDTO), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(authService.loginUser(loginDTO));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Boolean> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO, HttpServletRequest request) {
        return authService.resetPasswordWithResponse(resetPasswordDTO, request);
    }

    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifyPassword(@Valid @RequestBody VerifyPasswordDTO verifyPasswordDTO, HttpServletRequest request) {
        return authService.verifyPasswordWithResponse(verifyPasswordDTO, request);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return authService.ping();
    }
}
