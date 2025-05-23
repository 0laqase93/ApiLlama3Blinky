package com.blinky.apillama3blinky.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class EventUpdateDTO {

    private Long id;

    @Size(max = 255, message = "El título no puede exceder los 255 caracteres")
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    @Size(max = 255, message = "La ubicación no puede exceder los 255 caracteres")
    private String location;

    @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
    private String description;

    // Default constructor
    public EventUpdateDTO() {
    }

    // Constructor without id
    public EventUpdateDTO(String title, LocalDateTime startTime, LocalDateTime endTime, 
                         String location, String description) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.description = description;
    }

    // Constructor with all fields (deprecated, use the constructor without id and set id separately)
    public EventUpdateDTO(Long id, String title, LocalDateTime startTime, LocalDateTime endTime, 
                         String location, String description) {
        this(title, startTime, endTime, location, description);
        this.id = id;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
