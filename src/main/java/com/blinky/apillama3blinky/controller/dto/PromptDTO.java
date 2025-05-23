package com.blinky.apillama3blinky.controller.dto;

import jakarta.validation.constraints.NotBlank;

public class PromptDTO {
    @NotBlank(message = "El prompt no puede estar vacío")
    private String prompt;
    private Long userId;
    private Long personalityId;

    public PromptDTO() {
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
