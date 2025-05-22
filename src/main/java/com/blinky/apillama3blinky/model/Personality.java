package com.blinky.apillama3blinky.model;

import jakarta.persistence.*;

@Entity
@Table(name = "personalities")
public class Personality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String basePrompt;

    @Column(nullable = false)
    private String description;

    // Default constructor
    public Personality() {
    }

    // Constructor with parameters
    public Personality(String name, String basePrompt, String description) {
        this.name = name;
        this.basePrompt = basePrompt;
        this.description = description;
    }

    // Getters and setters
    public Long getId() {
        return id;
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