package com.blinky.apillama3blinky.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    private String username;

    // Default constructor
    public RegisterDTO() {
    }

    // Constructor with fields
    public RegisterDTO(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }

    // Constructor without username for backward compatibility
    public RegisterDTO(String email, String password) {
        this.email = email;
        this.password = password;
        this.username = null; // Default value
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
