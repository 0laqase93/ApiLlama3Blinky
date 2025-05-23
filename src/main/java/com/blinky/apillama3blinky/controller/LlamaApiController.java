package com.blinky.apillama3blinky.controller;

import com.blinky.apillama3blinky.controller.dto.PromptDTO;
import com.blinky.apillama3blinky.controller.response.PromptResponse;
import com.blinky.apillama3blinky.exception.EventException;
import com.blinky.apillama3blinky.security.JwtUtil;
import com.blinky.apillama3blinky.service.LlamaApiService;
import com.blinky.apillama3blinky.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/llama")
public class LlamaApiController {

    private final LlamaApiService llamaApiService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Autowired
    public LlamaApiController(LlamaApiService llamaApiService, JwtUtil jwtUtil, UserService userService) {
        this.llamaApiService = llamaApiService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/send_prompt")
    public ResponseEntity<PromptResponse> sendPrompt(@Valid @RequestBody PromptDTO promptDTO, HttpServletRequest request) {
        // Extract userId from token
        Long userId = jwtUtil.getUserIdFromRequest(request);

        // Set the userId in the promptDTO
        promptDTO.setUserId(userId);

        return ResponseEntity.ok(llamaApiService.sendPrompt(promptDTO));
    }
}
