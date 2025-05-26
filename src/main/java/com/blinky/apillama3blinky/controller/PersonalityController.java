package com.blinky.apillama3blinky.controller;

import com.blinky.apillama3blinky.controller.dto.PersonalityDTO;
import com.blinky.apillama3blinky.controller.response.PersonalityResponseDTO;
import com.blinky.apillama3blinky.service.PersonalityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for personality management API endpoints.
 * Provides CRUD operations for personality entities used by the AI model.
 */
@RestController
@RequestMapping("/api/personalities")
public class PersonalityController {

    private final PersonalityService personalityService;

    public PersonalityController(PersonalityService personalityService) {
        this.personalityService = personalityService;
    }

    /**
     * Retrieves all personalities available in the system.
     * 
     * @return Response entity with a list of all personalities
     */
    @GetMapping
    public ResponseEntity<List<PersonalityResponseDTO>> getAllPersonalities() {
        return ResponseEntity.ok(personalityService.getAllPersonalitiesAsResponseDTOs());
    }

    /**
     * Retrieves a specific personality by its ID.
     * 
     * @param id The ID of the personality to retrieve
     * @return Response entity with the requested personality's details
     */
    @GetMapping("/{id}")
    public ResponseEntity<PersonalityResponseDTO> getPersonalityById(@PathVariable Long id) {
        return ResponseEntity.ok(personalityService.getPersonalityByIdAsResponseDTO(id));
    }

    /**
     * Creates a new personality in the system.
     * 
     * @param personalityDTO Data transfer object containing the new personality's details
     * @return Response entity with the created personality's details and HTTP 201 Created status
     */
    @PostMapping
    public ResponseEntity<PersonalityResponseDTO> createPersonality(@Valid @RequestBody PersonalityDTO personalityDTO) {
        return new ResponseEntity<>(personalityService.createPersonalityFromDTO(personalityDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing personality's information.
     * 
     * @param id The ID of the personality to update
     * @param personalityDTO Data transfer object containing the updated personality details
     * @return Response entity with the updated personality's details
     */
    @PutMapping("/{id}")
    public ResponseEntity<PersonalityResponseDTO> updatePersonality(@PathVariable Long id, @Valid @RequestBody PersonalityDTO personalityDTO) {
        return ResponseEntity.ok(personalityService.updatePersonalityFromDTO(id, personalityDTO));
    }

    /**
     * Deletes a personality from the system.
     * 
     * @param id The ID of the personality to delete
     * @return Response entity with appropriate status code
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePersonality(@PathVariable Long id) {
        return personalityService.deletePersonalityWithResponse(id);
    }
}
