package com.blinky.apillama3blinky.service;

import com.blinky.apillama3blinky.controller.dto.PersonalityDTO;
import com.blinky.apillama3blinky.controller.response.PersonalityResponseDTO;
import com.blinky.apillama3blinky.exception.ResourceNotFoundException;
import com.blinky.apillama3blinky.mapping.PersonalityMapper;
import com.blinky.apillama3blinky.model.Personality;
import com.blinky.apillama3blinky.repository.PersonalityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsible for managing personality profiles used by the AI model.
 * Provides methods for creating, retrieving, updating, and deleting personalities.
 */
@Service
public class PersonalityService {

    private final PersonalityRepository personalityRepository;

    public PersonalityService(PersonalityRepository personalityRepository) {
        this.personalityRepository = personalityRepository;
    }

    /**
     * Retrieves all personality profiles from the database.
     * 
     * @return A list of all personality entities
     */
    @Transactional(readOnly = true)
    public List<Personality> getAllPersonalities() {
        return personalityRepository.findAll();
    }

    /**
     * Retrieves all personality profiles and converts them to DTOs.
     * 
     * @return A list of personality response DTOs
     */
    @Transactional(readOnly = true)
    public List<PersonalityResponseDTO> getAllPersonalitiesAsResponseDTOs() {
        List<Personality> personalities = getAllPersonalities();
        return PersonalityMapper.toResponseDTOList(personalities);
    }

    /**
     * Retrieves a specific personality by its ID.
     * 
     * @param id The ID of the personality to retrieve
     * @return The personality entity
     * @throws ResourceNotFoundException if no personality is found with the given ID
     */
    @Transactional(readOnly = true)
    public Personality getPersonalityById(Long id) {
        return personalityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personalidad no encontrada con ID: " + id));
    }

    /**
     * Retrieves a specific personality by its ID and converts it to a DTO.
     * 
     * @param id The ID of the personality to retrieve
     * @return The personality response DTO
     * @throws ResourceNotFoundException if no personality is found with the given ID
     */
    @Transactional(readOnly = true)
    public PersonalityResponseDTO getPersonalityByIdAsResponseDTO(Long id) {
        Personality personality = getPersonalityById(id);
        return PersonalityMapper.toResponseDTO(personality);
    }

    /**
     * Retrieves a specific personality by its name.
     * 
     * @param name The name of the personality to retrieve
     * @return The personality entity
     * @throws ResourceNotFoundException if no personality is found with the given name
     */
    @Transactional(readOnly = true)
    public Personality getPersonalityByName(String name) {
        return personalityRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Personalidad no encontrada con nombre: " + name));
    }

    /**
     * Retrieves a personality by either ID or name.
     * Prioritizes ID if both are provided.
     * 
     * @param id The ID of the personality to retrieve (optional)
     * @param name The name of the personality to retrieve (optional)
     * @return The personality entity
     * @throws ResourceNotFoundException if no personality is found with the given ID or name
     * @throws IllegalArgumentException if neither ID nor name is provided
     */
    @Transactional(readOnly = true)
    public Personality getPersonality(Long id, String name) {
        // If ID is provided, use it to find the personality
        if (id != null) {
            return getPersonalityById(id);
        } 
        // If name is provided, use it to find the personality
        else if (name != null && !name.isEmpty()) {
            return getPersonalityByName(name);
        } 
        // If neither ID nor name is provided, throw an exception
        else {
            throw new IllegalArgumentException("Se debe proporcionar un ID o un nombre de personalidad");
        }
    }

    /**
     * Creates a new personality profile in the database.
     * 
     * @param personality The personality entity to create
     * @return The created personality entity with generated ID
     */
    @Transactional
    public Personality createPersonality(Personality personality) {
        return personalityRepository.save(personality);
    }

    /**
     * Creates a new personality profile from a DTO.
     * 
     * @param personalityDTO The DTO containing the personality data
     * @return A response DTO representing the created personality
     */
    @Transactional
    public PersonalityResponseDTO createPersonalityFromDTO(PersonalityDTO personalityDTO) {
        // Convert DTO to entity
        Personality personality = PersonalityMapper.toEntity(personalityDTO);
        // Save the entity
        Personality createdPersonality = createPersonality(personality);
        // Convert back to response DTO
        return PersonalityMapper.toResponseDTO(createdPersonality);
    }

    /**
     * Updates an existing personality profile.
     * 
     * @param id The ID of the personality to update
     * @param personalityDetails The personality entity containing updated details
     * @return The updated personality entity
     * @throws ResourceNotFoundException if no personality is found with the given ID
     */
    @Transactional
    public Personality updatePersonality(Long id, Personality personalityDetails) {
        // Find the existing personality
        Personality personality = getPersonalityById(id);

        // Update the fields
        personality.setName(personalityDetails.getName());
        personality.setBasePrompt(personalityDetails.getBasePrompt());
        personality.setDescription(personalityDetails.getDescription());

        // Save the updated entity
        return personalityRepository.save(personality);
    }

    /**
     * Updates an existing personality profile from a DTO.
     * 
     * @param id The ID of the personality to update
     * @param personalityDTO The DTO containing the updated personality data
     * @return A response DTO representing the updated personality
     * @throws ResourceNotFoundException if no personality is found with the given ID
     */
    @Transactional
    public PersonalityResponseDTO updatePersonalityFromDTO(Long id, PersonalityDTO personalityDTO) {
        // Find the existing personality
        Personality existingPersonality = getPersonalityById(id);
        // Update the entity from DTO
        Personality updatedPersonality = PersonalityMapper.updateEntityFromDTO(existingPersonality, personalityDTO);
        // Save the updated entity
        Personality savedPersonality = updatePersonality(id, updatedPersonality);
        // Convert back to response DTO
        return PersonalityMapper.toResponseDTO(savedPersonality);
    }

    /**
     * Deletes a personality profile from the database.
     * 
     * @param id The ID of the personality to delete
     * @throws ResourceNotFoundException if no personality is found with the given ID
     */
    @Transactional
    public void deletePersonality(Long id) {
        // Find the personality to ensure it exists
        Personality personality = getPersonalityById(id);
        // Delete the personality
        personalityRepository.delete(personality);
    }

    /**
     * Deletes a personality profile and returns an appropriate HTTP response.
     * 
     * @param id The ID of the personality to delete
     * @return A response entity with HTTP 204 No Content status
     * @throws ResourceNotFoundException if no personality is found with the given ID
     */
    @Transactional
    public ResponseEntity<Void> deletePersonalityWithResponse(Long id) {
        deletePersonality(id);
        return ResponseEntity.noContent().build();
    }
}
