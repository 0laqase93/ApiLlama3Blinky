package com.blinky.apillama3blinky.repository;

import com.blinky.apillama3blinky.model.Personality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonalityRepository extends JpaRepository<Personality, Long> {
    
    /**
     * Find a personality by its name
     * @param name the name of the personality
     * @return an Optional containing the personality if found
     */
    Optional<Personality> findByName(String name);
}