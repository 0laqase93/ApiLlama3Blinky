package com.blinky.apillama3blinky.controller;

import com.blinky.apillama3blinky.controller.dto.PromptDTO;
import com.blinky.apillama3blinky.controller.response.PromptResponse;
import com.blinky.apillama3blinky.security.JwtUtil;
import com.blinky.apillama3blinky.service.LlamaApiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for the Llama AI API endpoints.
 * Provides endpoints for sending prompts to the AI model and managing conversations.
 */
@Controller
@RequestMapping("/api/llama")
public class LlamaApiController {

    private final LlamaApiService llamaApiService;
    private final JwtUtil jwtUtil;

    @Autowired
    public LlamaApiController(LlamaApiService llamaApiService, JwtUtil jwtUtil) {
        this.llamaApiService = llamaApiService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Endpoint for sending a prompt to the AI model.
     * 
     * @param promptDTO The prompt data transfer object containing the user's message and optional personality ID
     * @param request The HTTP request containing the JWT token for user authentication
     * @return A response entity containing the AI's reply
     */
    @PostMapping("/send_prompt")
    public ResponseEntity<PromptResponse> sendPrompt(@Valid @RequestBody PromptDTO promptDTO, HttpServletRequest request) {
        // Extract the user ID from the JWT token
        Long userId = jwtUtil.getUserIdFromRequest(request);

        // Process the prompt and return the AI response
        return ResponseEntity.ok(llamaApiService.sendPrompt(promptDTO, userId));
    }

    /**
     * Endpoint for clearing a user's conversation history.
     * 
     * @param request The HTTP request containing the JWT token for user authentication
     * @return An empty response with HTTP 200 OK status
     */
    @PostMapping("/clear_conversation")
    public ResponseEntity<Void> clearConversation(HttpServletRequest request) {
        // Extract the user ID from the JWT token
        Long userId = jwtUtil.getUserIdFromRequest(request);

        // Clear the user's conversation history
        llamaApiService.clearConversation(userId.toString());
        return ResponseEntity.ok().build();
    }
}
