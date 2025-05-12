package com.blinky.apillama3blinky.repository;

import com.blinky.apillama3blinky.model.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMessageRepository extends JpaRepository<UserMessage, Long> {
    List<UserMessage> findByConversationId(Long conversationId);
}