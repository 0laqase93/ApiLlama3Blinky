package com.blinky.apillama3blinky.controller;

import com.blinky.apillama3blinky.controller.dto.PromptDTO;
import com.blinky.apillama3blinky.controller.response.PromptResponse;
import com.blinky.apillama3blinky.service.LlamaApiService;
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

    @Autowired
    private final LlamaApiService llamaApiService;

    public LlamaApiController(LlamaApiService llamaApiService) {
        this.llamaApiService = llamaApiService;
    }

    @PostMapping("/send_prompt")
    public ResponseEntity<PromptResponse> sendPrompt(@Valid @RequestBody PromptDTO promptDTO) {
        return ResponseEntity.ok(llamaApiService.sendPrompt(promptDTO));
    }
}
