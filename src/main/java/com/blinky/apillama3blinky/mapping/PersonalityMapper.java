package com.blinky.apillama3blinky.mapping;

import com.blinky.apillama3blinky.controller.dto.PersonalityDTO;
import com.blinky.apillama3blinky.controller.response.PersonalityResponseDTO;
import com.blinky.apillama3blinky.model.Personality;

import java.util.List;
import java.util.stream.Collectors;

public class PersonalityMapper {

    public static PersonalityDTO toDTO(Personality personality) {
        if (personality == null) {
            return null;
        }

        return new PersonalityDTO(
                personality.getId(),
                personality.getName(),
                personality.getBasePrompt(),
                personality.getDescription()
        );
    }

    public static List<PersonalityDTO> toDTOList(List<Personality> personalities) {
        return personalities.stream()
                .map(PersonalityMapper::toDTO)
                .collect(Collectors.toList());
    }

    public static Personality toEntity(PersonalityDTO personalityDTO) {
        if (personalityDTO == null) {
            return null;
        }

        return new Personality(
                personalityDTO.getName(),
                personalityDTO.getBasePrompt(),
                personalityDTO.getDescription()
        );
    }

    public static PersonalityResponseDTO toResponseDTO(Personality personality) {
        if (personality == null) {
            return null;
        }

        return new PersonalityResponseDTO(
                personality.getId(),
                personality.getName(),
                personality.getBasePrompt(),
                personality.getDescription()
        );
    }

    public static List<PersonalityResponseDTO> toResponseDTOList(List<Personality> personalities) {
        return personalities.stream()
                .map(PersonalityMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public static Personality updateEntityFromDTO(Personality personality, PersonalityDTO personalityDTO) {
        if (personality == null || personalityDTO == null) {
            return personality;
        }

        personality.setName(personalityDTO.getName());
        personality.setBasePrompt(personalityDTO.getBasePrompt());
        personality.setDescription(personalityDTO.getDescription());

        return personality;
    }
}