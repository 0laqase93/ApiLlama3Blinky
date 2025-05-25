package com.blinky.apillama3blinky.repository;

import com.blinky.apillama3blinky.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByUserId(Long userId);

    @Query("SELECT e FROM Event e WHERE e.user.id = :userId AND LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Event> findByUserIdAndTitleContainingIgnoreCase(
            @Param("userId") Long userId,
            @Param("title") String title);

    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Event> findByTitleContainingIgnoreCase(@Param("title") String title);
}
