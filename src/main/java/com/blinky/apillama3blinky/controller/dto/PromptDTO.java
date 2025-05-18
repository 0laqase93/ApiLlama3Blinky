package com.blinky.apillama3blinky.controller.dto;

import jakarta.validation.constraints.NotBlank;

public class PromptDTO {
    @NotBlank
    private String prompt;
    private Long userId;

    // Constructor, getters y setters
    public PromptDTO(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
