package com.blinky.apillama3blinky.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PersonalityDTO {
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 255, message = "El nombre no puede exceder los 255 caracteres")
    private String name;

    @NotBlank(message = "El prompt base no puede estar vacío")
    private String basePrompt;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
    private String description;

    // Default constructor
    public PersonalityDTO() {
    }

    // Constructor with fields
    public PersonalityDTO(String name, String basePrompt, String description) {
        this.name = name;
        this.basePrompt = basePrompt;
        this.description = description;
    }

    // Constructor with all fields including id
    public PersonalityDTO(Long id, String name, String basePrompt, String description) {
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