package com.blinky.apillama3blinky.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private String location;

    @Column(length = 1000)
    private String description;

    // Default constructor
    public Event() {
    }

    // Constructor with required fields
    public Event(String title, LocalDateTime startTime, LocalDateTime endTime, User user) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.user = user;
    }

    // Constructor with all fields
    public Event(String title, LocalDateTime startTime, LocalDateTime endTime, User user, String location, String description) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.user = user;
        this.location = location;
        this.description = description;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    /**
     * Sets the ID of the event.
     * This method is critical for JPA to recognize an entity as existing during updates.
     */
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
