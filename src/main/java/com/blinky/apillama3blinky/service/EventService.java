package com.blinky.apillama3blinky.service;

import com.blinky.apillama3blinky.controller.dto.EventCreateDTO;
import com.blinky.apillama3blinky.controller.dto.EventUpdateDTO;
import com.blinky.apillama3blinky.exception.EventException;
import com.blinky.apillama3blinky.exception.ResourceNotFoundException;
import com.blinky.apillama3blinky.exception.UserNotFoundException;
import com.blinky.apillama3blinky.mapping.EventMapper;
import com.blinky.apillama3blinky.model.Event;
import com.blinky.apillama3blinky.model.User;
import com.blinky.apillama3blinky.repository.EventRepository;
import com.blinky.apillama3blinky.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Autowired
    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get all events for a specific user
     */
    @Transactional(readOnly = true)
    public List<Event> getAllEventsByUserId(Long userId) {
        return eventRepository.findByUserId(userId);
    }

    /**
     * Get a specific event by ID
     * Validates that the event belongs to the specified user
     */
    @Transactional(readOnly = true)
    public Event getEventById(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + eventId));

        // Verify that the event belongs to the user
        if (!event.getUser().getId().equals(userId)) {
            throw new EventException("No tienes permiso para acceder a este evento");
        }

        return event;
    }

    /**
     * Create a new event for a user
     * Validates that startTime is before endTime
     */
    @Transactional
    public Event createEvent(EventCreateDTO eventCreateDTO, Long userId) {
        // Validate date times
        validateEventTimes(eventCreateDTO.getStartTime(), eventCreateDTO.getEndTime());

        // Get the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        // Create and save the event
        Event event = EventMapper.toEntity(eventCreateDTO, user);
        return eventRepository.save(event);
    }

    /**
     * Update an existing event
     * Validates that the event belongs to the specified user
     * Validates that startTime is before endTime if both are provided
     */
    @Transactional
    public Event updateEvent(EventUpdateDTO eventUpdateDTO, Long userId) {
        // Get the existing event
        Event existingEvent = getEventById(eventUpdateDTO.getId(), userId);

        // Determine the start and end times for validation
        LocalDateTime startTime = eventUpdateDTO.getStartTime() != null ? 
                eventUpdateDTO.getStartTime() : existingEvent.getStartTime();
        LocalDateTime endTime = eventUpdateDTO.getEndTime() != null ? 
                eventUpdateDTO.getEndTime() : existingEvent.getEndTime();

        // Validate date times
        validateEventTimes(startTime, endTime);

        // Update the event
        EventMapper.updateEntityFromDTO(eventUpdateDTO, existingEvent);

        // Save and return the updated event
        return eventRepository.save(existingEvent);
    }

    /**
     * Delete an event
     * Validates that the event belongs to the specified user
     */
    @Transactional
    public void deleteEvent(Long eventId, Long userId) {
        // Get the event and verify ownership
        Event event = getEventById(eventId, userId);

        // Delete the event
        eventRepository.delete(event);
    }


    /**
     * Find events for a user by title
     */
    @Transactional(readOnly = true)
    public List<Event> findEventsByTitle(Long userId, String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new EventException("El título de búsqueda no puede estar vacío");
        }

        return eventRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title);
    }

    /**
     * Validate that startTime is before endTime
     */
    private void validateEventTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null) {
            throw new EventException("La fecha y hora de inicio son obligatorias");
        }

        if (endTime == null) {
            throw new EventException("La fecha y hora de finalización son obligatorias");
        }

        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new EventException("La fecha y hora de inicio deben ser anteriores a la fecha y hora de finalización");
        }
    }
}
