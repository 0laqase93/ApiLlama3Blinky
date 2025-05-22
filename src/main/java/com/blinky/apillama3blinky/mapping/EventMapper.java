package com.blinky.apillama3blinky.mapping;

import com.blinky.apillama3blinky.controller.dto.EventCreateDTO;
import com.blinky.apillama3blinky.controller.dto.EventDTO;
import com.blinky.apillama3blinky.controller.dto.EventUpdateDTO;
import com.blinky.apillama3blinky.model.Event;
import com.blinky.apillama3blinky.model.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between Event entities and DTOs
 */
public class EventMapper {

    /**
     * Convert Event entity to EventDTO
     */
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

    /**
     * Convert list of Event entities to list of EventDTOs
     */
    public static List<EventDTO> toDTOList(List<Event> events) {
        if (events == null) {
            return null;
        }

        return events.stream()
            .map(EventMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Create a new Event entity from EventCreateDTO and User
     */
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

    /**
     * Update an existing Event entity from EventUpdateDTO
     * Only updates fields that are not null in the DTO
     */
    public static void updateEntityFromDTO(EventUpdateDTO dto, Event event) {
        if (dto == null || event == null) {
            return;
        }

        // Ensure the ID is set - this is critical for JPA to recognize this as an update
        // rather than an insert operation
        event.setId(dto.getId());

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
