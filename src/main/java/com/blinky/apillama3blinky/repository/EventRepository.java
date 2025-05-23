package com.blinky.apillama3blinky.repository;

import com.blinky.apillama3blinky.model.Event;
import com.blinky.apillama3blinky.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Find all events for a specific user
     */
    List<Event> findByUser(User user);

    /**
     * Find all events for a specific user by user ID
     */
    List<Event> findByUserId(Long userId);


    /**
     * Find events for a specific user by title (case insensitive, partial match)
     */
    @Query("SELECT e FROM Event e WHERE e.user.id = :userId AND LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Event> findByUserIdAndTitleContainingIgnoreCase(
            @Param("userId") Long userId,
            @Param("title") String title);

    /**
     * Find all events by title (case insensitive, partial match)
     * Used by admin users to search across all events
     */
    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Event> findByTitleContainingIgnoreCase(@Param("title") String title);
}
