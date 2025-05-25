package com.blinky.apillama3blinky.api;

import com.blinky.apillama3blinky.controller.dto.PromptDTO;
import com.blinky.apillama3blinky.controller.response.PromptResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface LlamaApiControllerApi {

    ResponseEntity<PromptResponse> sendPrompt(
            @Valid @RequestBody PromptDTO promptDTO,
            HttpServletRequest request);
}