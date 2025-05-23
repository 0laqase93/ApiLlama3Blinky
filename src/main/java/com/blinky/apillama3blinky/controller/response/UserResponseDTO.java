package com.blinky.apillama3blinky.controller.response;

/**
 * DTO for User responses without sensitive information like passwords.
 * This class is used for returning user data to clients.
 */
public class UserResponseDTO {
    private Long id;
    private String email;
    private boolean isAdmin;
    private String username;

    // Default constructor
    public UserResponseDTO() {
    }

    // Constructor with fields
    public UserResponseDTO(Long id, String email, boolean isAdmin, String username) {
        this.id = id;
        this.email = email;
        this.isAdmin = isAdmin;
        this.username = username;
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