package com.blinky.apillama3blinky.cache;

import com.blinky.apillama3blinky.model.Personality;
import com.blinky.apillama3blinky.service.PersonalityService;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cache for Personality entities to reduce database queries.
 * Loads all personalities at startup and provides methods to access them.
 */
@Component
public class PersonalityCache {

    private final PersonalityService personalityService;
    private final Map<Long, Personality> personalitiesById = new HashMap<>();

    public PersonalityCache(PersonalityService personalityService) {
        this.personalityService = personalityService;
    }

    /**
     * Loads all personalities from the database into the cache at application startup.
     */
    @PostConstruct
    public void init() {
        List<Personality> personalities = personalityService.getAllPersonalities();
        personalities.forEach(personality -> personalitiesById.put(personality.getId(), personality));
    }

    /**
     * Gets a personality by ID from the cache.
     *
     * @param id the personality ID
     * @return the personality, or empty if not found
     */
    public Optional<Personality> getPersonalityById(Long id) {
        return Optional.ofNullable(personalitiesById.get(id));
    }

    /**
     * Gets the first personality in the cache, if any exist.
     *
     * @return the first personality, or empty if none exist
     */
    public Optional<Personality> getFirstPersonality() {
        return personalitiesById.values().stream().findFirst();
    }

    /**
     * Gets all personalities from the cache.
     *
     * @return a list of all personalities
     */
    public List<Personality> getAllPersonalities() {
        return List.copyOf(personalitiesById.values());
    }

    /**
     * Refreshes the cache by reloading all personalities from the database.
     * This can be called when personalities are added, updated, or removed.
     */
    public void refresh() {
        personalitiesById.clear();
        init();
    }
}