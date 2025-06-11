package com.blinky.apillama3blinky.mapping;

import com.blinky.apillama3blinky.controller.dto.EventCreateDTO;
import com.blinky.apillama3blinky.controller.dto.EventDTO;
import com.blinky.apillama3blinky.controller.dto.EventUpdateDTO;
import com.blinky.apillama3blinky.model.Event;
import com.blinky.apillama3blinky.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class EventMapper {

    public static EventDTO toDTO(Event event) {
        if (event == null) {
            return null;
        }

        return new EventDTO(
                event.getId(),
                event.getTitle(),
                event.getStartTime(),
                event.getEndTime(),
                event.getUser() != null ? event.getUser().getId() : null,
                event.getLocation(),
                event.getDescription()
        );
    }

    public static EventDTO createDTOFromCreateDTO(EventCreateDTO createDTO, Long userId) {
        if (createDTO == null) {
            return null;
        }

        return new EventDTO(
                null, // No ID since the event hasn't been created yet
                createDTO.getTitle(),
                createDTO.getStartTime(),
                createDTO.getEndTime(),
                userId,
                createDTO.getLocation(),
                createDTO.getDescription()
        );
    }

    public static List<EventDTO> toDTOList(List<Event> events) {
        if (events == null) {
            return null;
        }

        return events.stream()
                .map(EventMapper::toDTO)
                .collect(Collectors.toList());
    }

    public static Event toEntity(EventCreateDTO dto, User user) {
        if (dto == null) {
            return null;
        }

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setUser(user);
        event.setLocation(dto.getLocation());
        event.setDescription(dto.getDescription());

        return event;
    }

    public static void updateEntityFromDTO(EventUpdateDTO dto, Event event) {
        if (dto == null || event == null) {
            return;
        }

        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }

        if (dto.getStartTime() != null) {
            event.setStartTime(dto.getStartTime());
        }

        if (dto.getEndTime() != null) {
            event.setEndTime(dto.getEndTime());
        }

        if (dto.getLocation() != null) {
            event.setLocation(dto.getLocation());
        }

        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
    }
}
