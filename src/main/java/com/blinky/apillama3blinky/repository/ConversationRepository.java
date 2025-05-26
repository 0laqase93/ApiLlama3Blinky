package com.blinky.apillama3blinky.repository;

import com.blinky.apillama3blinky.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Finds a conversation by ID and eagerly fetches its messages and responses.
     * This avoids N+1 queries when accessing the conversation history.
     *
     * @param convId the conversation ID
     * @return the conversation with all messages and responses loaded
     */
    @Query("SELECT DISTINCT c FROM Conversation c " +
           "LEFT JOIN FETCH c.userMessages um " +
           "LEFT JOIN FETCH c.aiResponses ar " +
           "LEFT JOIN FETCH ar.userMessage " +
           "WHERE c.id = :convId")
    Optional<Conversation> findWithMessagesAndResponses(@Param("convId") Long convId);
}
