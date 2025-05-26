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

    @PostMapping("/send_prompt")
    public ResponseEntity<PromptResponse> sendPrompt(@Valid @RequestBody PromptDTO promptDTO, HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromRequest(request);

        return ResponseEntity.ok(llamaApiService.sendPrompt(promptDTO, userId));
    }

    @PostMapping("/clear_conversation")
    public ResponseEntity<Void> clearConversation(HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromRequest(request);

        llamaApiService.clearConversation(userId.toString());
        return ResponseEntity.ok().build();
    }
}
