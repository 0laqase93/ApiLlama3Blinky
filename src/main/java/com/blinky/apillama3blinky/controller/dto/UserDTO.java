package com.blinky.apillama3blinky.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDTO {
    private Long id;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    private boolean isAdmin;

    private String username;

    // Default constructor
    public UserDTO() {
    }

    // Constructor with fields
    public UserDTO(Long id, String email, String password, boolean isAdmin, String username) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.username = username;
    }

    // Constructor with fields without username
    public UserDTO(Long id, String email, String password, boolean isAdmin) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.username = null; // Default value
    }

    // Constructor without isAdmin for backward compatibility
    public UserDTO(Long id, String email, String password) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.isAdmin = false; // Default value
        this.username = null; // Default value
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
