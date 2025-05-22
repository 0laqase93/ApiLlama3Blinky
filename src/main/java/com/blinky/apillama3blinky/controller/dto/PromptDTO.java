package com.blinky.apillama3blinky.controller.dto;

import jakarta.validation.constraints.NotBlank;

public class PromptDTO {
    @NotBlank
    private String prompt;
    private Long userId;
    private Long personalityId;

    // Constructor, getters y setters
    public PromptDTO() {
        // Default constructor for JSON deserialization
    }

    public PromptDTO(String prompt) {
        this.prompt = prompt;
    }

    public PromptDTO(String prompt, Long personalityId) {
        this.prompt = prompt;
        this.personalityId = personalityId;
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

    public Long getPersonalityId() {
        return personalityId;
    }

    public void setPersonalityId(Long personalityId) {
        this.personalityId = personalityId;
    }
}
