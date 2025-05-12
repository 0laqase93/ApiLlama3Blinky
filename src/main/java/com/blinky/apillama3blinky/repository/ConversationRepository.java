package com.blinky.apillama3blinky.repository;

import com.blinky.apillama3blinky.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
}
