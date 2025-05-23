package com.blinky.apillama3blinky.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UserUpdateDTO {
    @Email(message = "El formato del email no es válido")
    private String email;

    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    private boolean isAdmin;

    private String username;

    // Default constructor
    public UserUpdateDTO() {
    }

    // Constructor without id
    public UserUpdateDTO(String email, String password, boolean isAdmin, String username) {
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.username = username;
    }

    // Constructor without isAdmin and id for backward compatibility
    public UserUpdateDTO(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.isAdmin = false; // Default value
        this.username = username;
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

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
