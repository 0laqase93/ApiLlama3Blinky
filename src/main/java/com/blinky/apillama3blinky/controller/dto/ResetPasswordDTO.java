package com.blinky.apillama3blinky.controller.dto;

import jakarta.validation.constraints.NotBlank;

public class ResetPasswordDTO {

    @NotBlank
    private String newPassword;

    // Default constructor
    public ResetPasswordDTO() {
    }

    // Constructor with parameters
    public ResetPasswordDTO(String newPassword) {
        this.newPassword = newPassword;
    }

    // Getters and setters
    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
