package com.blinky.apillama3blinky.repository;

import com.blinky.apillama3blinky.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
