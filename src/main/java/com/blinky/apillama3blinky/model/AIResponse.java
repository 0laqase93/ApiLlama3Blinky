package com.blinky.apillama3blinky.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AIResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;
    
    @OneToOne
    @JoinColumn(name = "user_message_id")
    private UserMessage userMessage;

    public AIResponse() {
    }

    public AIResponse(String content) {
        this.content = content;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }
    
    public UserMessage getUserMessage() {
        return userMessage;
    }
    
    public void setUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
    }
}