package com.blinky.apillama3blinky.controller;

import com.blinky.apillama3blinky.model.Personality;
import com.blinky.apillama3blinky.service.PersonalityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personalities")
public class PersonalityController {

    private final PersonalityService personalityService;

    public PersonalityController(PersonalityService personalityService) {
        this.personalityService = personalityService;
    }

    /**
     * Get all personalities
     * @return list of all personalities
     */
    @GetMapping
    public ResponseEntity<List<Personality>> getAllPersonalities() {
        return ResponseEntity.ok(personalityService.getAllPersonalities());
    }

    /**
     * Get a personality by its ID
     * @param id the ID of the personality
     * @return the personality
     */
    @GetMapping("/{id}")
    public ResponseEntity<Personality> getPersonalityById(@PathVariable Long id) {
        return ResponseEntity.ok(personalityService.getPersonalityById(id));
    }

    /**
     * Create a new personality
     * @param personality the personality to create
     * @return the created personality
     */
    @PostMapping
    public ResponseEntity<Personality> createPersonality(@Valid @RequestBody Personality personality) {
        return new ResponseEntity<>(personalityService.createPersonality(personality), HttpStatus.CREATED);
    }

    /**
     * Update an existing personality
     * @param id the ID of the personality to update
     * @param personalityDetails the updated personality details
     * @return the updated personality
     */
    @PutMapping("/{id}")
    public ResponseEntity<Personality> updatePersonality(@PathVariable Long id, @Valid @RequestBody Personality personalityDetails) {
        return ResponseEntity.ok(personalityService.updatePersonality(id, personalityDetails));
    }

    /**
     * Delete a personality
     * @param id the ID of the personality to delete
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePersonality(@PathVariable Long id) {
        personalityService.deletePersonality(id);
        return ResponseEntity.noContent().build();
    }
}