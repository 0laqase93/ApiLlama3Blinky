package com.blinky.apillama3blinky.service;

import com.blinky.apillama3blinky.exception.ResourceNotFoundException;
import com.blinky.apillama3blinky.model.Personality;
import com.blinky.apillama3blinky.repository.PersonalityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PersonalityService {

    private final PersonalityRepository personalityRepository;

    public PersonalityService(PersonalityRepository personalityRepository) {
        this.personalityRepository = personalityRepository;
    }

    /**
     * Get all personalities
     * @return list of all personalities
     */
    public List<Personality> getAllPersonalities() {
        return personalityRepository.findAll();
    }

    /**
     * Get a personality by its ID
     * @param id the ID of the personality
     * @return the personality
     * @throws ResourceNotFoundException if the personality is not found
     */
    public Personality getPersonalityById(Long id) {
        return personalityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personalidad no encontrada con ID: " + id));
    }

    /**
     * Get a personality by its name
     * @param name the name of the personality
     * @return the personality
     * @throws ResourceNotFoundException if the personality is not found
     */
    public Personality getPersonalityByName(String name) {
        return personalityRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Personalidad no encontrada con nombre: " + name));
    }

    /**
     * Get a personality by its ID or name
     * @param id the ID of the personality (can be null)
     * @param name the name of the personality (can be null)
     * @return the personality
     * @throws ResourceNotFoundException if the personality is not found
     * @throws IllegalArgumentException if both id and name are null
     */
    public Personality getPersonality(Long id, String name) {
        if (id != null) {
            return getPersonalityById(id);
        } else if (name != null && !name.isEmpty()) {
            return getPersonalityByName(name);
        } else {
            throw new IllegalArgumentException("Se debe proporcionar un ID o un nombre de personalidad");
        }
    }

    /**
     * Create a new personality
     * @param personality the personality to create
     * @return the created personality
     */
    @Transactional
    public Personality createPersonality(Personality personality) {
        return personalityRepository.save(personality);
    }

    /**
     * Update an existing personality
     * @param id the ID of the personality to update
     * @param personalityDetails the updated personality details
     * @return the updated personality
     * @throws ResourceNotFoundException if the personality is not found
     */
    @Transactional
    public Personality updatePersonality(Long id, Personality personalityDetails) {
        Personality personality = getPersonalityById(id);
        
        personality.setName(personalityDetails.getName());
        personality.setBasePrompt(personalityDetails.getBasePrompt());
        personality.setDescription(personalityDetails.getDescription());
        
        return personalityRepository.save(personality);
    }

    /**
     * Delete a personality
     * @param id the ID of the personality to delete
     * @throws ResourceNotFoundException if the personality is not found
     */
    @Transactional
    public void deletePersonality(Long id) {
        Personality personality = getPersonalityById(id);
        personalityRepository.delete(personality);
    }
}