package com.blinky.apillama3blinky.api;

import com.blinky.apillama3blinky.controller.dto.PersonalityDTO;
import com.blinky.apillama3blinky.controller.response.PersonalityResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface PersonalityControllerApi {

    ResponseEntity<List<PersonalityResponseDTO>> getAllPersonalities();

    ResponseEntity<PersonalityResponseDTO> getPersonalityById(
            @PathVariable Long id);

    ResponseEntity<PersonalityResponseDTO> createPersonality(
            @Valid @RequestBody PersonalityDTO personalityDTO);

    ResponseEntity<PersonalityResponseDTO> updatePersonality(
            @PathVariable Long id,
            @Valid @RequestBody PersonalityDTO personalityDTO);

    ResponseEntity<Void> deletePersonality(
            @PathVariable Long id);
}