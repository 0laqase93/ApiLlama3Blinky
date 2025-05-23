package com.blinky.apillama3blinky.controller;

import com.blinky.apillama3blinky.controller.dto.PersonalityDTO;
import com.blinky.apillama3blinky.controller.response.PersonalityResponseDTO;
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

    @GetMapping
    public ResponseEntity<List<PersonalityResponseDTO>> getAllPersonalities() {
        return ResponseEntity.ok(personalityService.getAllPersonalitiesAsResponseDTOs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonalityResponseDTO> getPersonalityById(@PathVariable Long id) {
        return ResponseEntity.ok(personalityService.getPersonalityByIdAsResponseDTO(id));
    }

    @PostMapping
    public ResponseEntity<PersonalityResponseDTO> createPersonality(@Valid @RequestBody PersonalityDTO personalityDTO) {
        return new ResponseEntity<>(personalityService.createPersonalityFromDTO(personalityDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonalityResponseDTO> updatePersonality(@PathVariable Long id, @Valid @RequestBody PersonalityDTO personalityDTO) {
        return ResponseEntity.ok(personalityService.updatePersonalityFromDTO(id, personalityDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePersonality(@PathVariable Long id) {
        return personalityService.deletePersonalityWithResponse(id);
    }
}
