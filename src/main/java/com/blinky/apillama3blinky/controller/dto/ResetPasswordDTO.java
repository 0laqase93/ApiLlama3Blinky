package com.blinky.apillama3blinky.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordDTO {

    @NotBlank(message = "La nueva contraseña no puede estar vacía")
    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
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
