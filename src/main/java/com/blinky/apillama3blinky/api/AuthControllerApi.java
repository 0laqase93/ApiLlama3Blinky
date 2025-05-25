package com.blinky.apillama3blinky.api;

import com.blinky.apillama3blinky.controller.dto.LoginDTO;
import com.blinky.apillama3blinky.controller.dto.RegisterDTO;
import com.blinky.apillama3blinky.controller.dto.ResetPasswordDTO;
import com.blinky.apillama3blinky.controller.dto.VerifyPasswordDTO;
import com.blinky.apillama3blinky.controller.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthControllerApi {

    @PostMapping("/register")
    ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterDTO registerDTO);

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginDTO loginDTO);

    @PostMapping("/reset-password")
    ResponseEntity<Boolean> resetPassword(
            @Valid @RequestBody ResetPasswordDTO resetPasswordDTO,
            HttpServletRequest request);

    @PostMapping("/verify-password")
    ResponseEntity<Boolean> verifyPassword(
            @Valid @RequestBody VerifyPasswordDTO verifyPasswordDTO,
            HttpServletRequest request);

    @GetMapping("/ping")
    ResponseEntity<String> ping();
}
