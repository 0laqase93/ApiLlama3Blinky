package com.blinky.apillama3blinky.controller.dto;

import jakarta.validation.constraints.NotBlank;

public class VerifyPasswordDTO {

    @NotBlank
    private String password;

    // Default constructor
    public VerifyPasswordDTO() {
    }

    // Constructor with parameters
    public VerifyPasswordDTO(String password) {
        this.password = password;
    }

    // Getters and setters
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
