package com.blinky.apillama3blinky.repository;

import com.blinky.apillama3blinky.model.AIResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIResponseRepository extends JpaRepository<AIResponse, Long> {
    List<AIResponse> findByConversationId(Long conversationId);
    Optional<AIResponse> findByUserMessageId(Long userMessageId);
}