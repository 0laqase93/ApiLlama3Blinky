package com.blinky.apillama3blinky.controller;

import com.blinky.apillama3blinky.controller.dto.LoginDTO;
import com.blinky.apillama3blinky.controller.dto.RegisterDTO;
import com.blinky.apillama3blinky.controller.dto.UserDTO;
import com.blinky.apillama3blinky.controller.response.AuthResponse;
import com.blinky.apillama3blinky.exception.UserNotFoundException;
import com.blinky.apillama3blinky.mapping.UserMapper;
import com.blinky.apillama3blinky.model.User;
import com.blinky.apillama3blinky.service.AuthService;
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
        AuthResponse authResponse = authService.registerUserFromDTO(registerDTO);
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginDTO loginDTO) {
        AuthResponse authResponse = authService.loginUser(loginDTO.getEmail(), loginDTO.getPassword());
        return ResponseEntity.ok(authResponse);
    }
}
