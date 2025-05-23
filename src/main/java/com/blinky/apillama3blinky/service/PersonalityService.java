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

@Service
public class PersonalityService {

    private final PersonalityRepository personalityRepository;

    public PersonalityService(PersonalityRepository personalityRepository) {
        this.personalityRepository = personalityRepository;
    }

    @Transactional(readOnly = true)
    public List<Personality> getAllPersonalities() {
        return personalityRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PersonalityResponseDTO> getAllPersonalitiesAsResponseDTOs() {
        List<Personality> personalities = getAllPersonalities();
        return PersonalityMapper.toResponseDTOList(personalities);
    }

    @Transactional(readOnly = true)
    public Personality getPersonalityById(Long id) {
        return personalityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personalidad no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public PersonalityResponseDTO getPersonalityByIdAsResponseDTO(Long id) {
        Personality personality = getPersonalityById(id);
        return PersonalityMapper.toResponseDTO(personality);
    }

    @Transactional(readOnly = true)
    public Personality getPersonalityByName(String name) {
        return personalityRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Personalidad no encontrada con nombre: " + name));
    }

    @Transactional(readOnly = true)
    public Personality getPersonality(Long id, String name) {
        if (id != null) {
            return getPersonalityById(id);
        } else if (name != null && !name.isEmpty()) {
            return getPersonalityByName(name);
        } else {
            throw new IllegalArgumentException("Se debe proporcionar un ID o un nombre de personalidad");
        }
    }

    @Transactional
    public Personality createPersonality(Personality personality) {
        return personalityRepository.save(personality);
    }

    @Transactional
    public PersonalityResponseDTO createPersonalityFromDTO(PersonalityDTO personalityDTO) {
        Personality personality = PersonalityMapper.toEntity(personalityDTO);
        Personality createdPersonality = createPersonality(personality);
        return PersonalityMapper.toResponseDTO(createdPersonality);
    }

    @Transactional
    public Personality updatePersonality(Long id, Personality personalityDetails) {
        Personality personality = getPersonalityById(id);

        personality.setName(personalityDetails.getName());
        personality.setBasePrompt(personalityDetails.getBasePrompt());
        personality.setDescription(personalityDetails.getDescription());

        return personalityRepository.save(personality);
    }

    @Transactional
    public PersonalityResponseDTO updatePersonalityFromDTO(Long id, PersonalityDTO personalityDTO) {
        Personality existingPersonality = getPersonalityById(id);
        Personality updatedPersonality = PersonalityMapper.updateEntityFromDTO(existingPersonality, personalityDTO);
        Personality savedPersonality = updatePersonality(id, updatedPersonality);
        return PersonalityMapper.toResponseDTO(savedPersonality);
    }

    @Transactional
    public void deletePersonality(Long id) {
        Personality personality = getPersonalityById(id);
        personalityRepository.delete(personality);
    }

    @Transactional
    public ResponseEntity<Void> deletePersonalityWithResponse(Long id) {
        deletePersonality(id);
        return ResponseEntity.noContent().build();
    }
}
