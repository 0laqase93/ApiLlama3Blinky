package com.blinky.apillama3blinky.controller.response;

/**
 * DTO for Personality responses.
 * This class is used for returning personality data to clients.
 */
public class PersonalityResponseDTO {
    private Long id;
    private String name;
    private String basePrompt;
    private String description;

    // Default constructor
    public PersonalityResponseDTO() {
    }

    // Constructor with fields
    public PersonalityResponseDTO(Long id, String name, String basePrompt, String description) {
        this.id = id;
        this.name = name;
        this.basePrompt = basePrompt;
        this.description = description;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBasePrompt() {
        return basePrompt;
    }

    public void setBasePrompt(String basePrompt) {
        this.basePrompt = basePrompt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}