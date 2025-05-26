package com.blinky.apillama3blinky.service;

import com.blinky.apillama3blinky.controller.dto.EventCreateDTO;
import com.blinky.apillama3blinky.controller.dto.EventUpdateDTO;
import com.blinky.apillama3blinky.exception.EventException;
import com.blinky.apillama3blinky.exception.ResourceNotFoundException;
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

/**
 * Service responsible for event management.
 * Provides methods for creating, retrieving, updating, and deleting events,
 * with user-specific access controls and admin capabilities.
 */
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
     * Retrieves all events belonging to a specific user.
     * 
     * @param userId The ID of the user whose events to retrieve
     * @return A list of events belonging to the user
     */
    @Transactional(readOnly = true)
    public List<Event> getAllEventsByUserId(Long userId) {
        return eventRepository.findByUserId(userId);
    }

    /**
     * Retrieves all events in the system.
     * Typically used by administrators to view all events.
     * 
     * @return A list of all events
     */
    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    /**
     * Retrieves a specific event by its ID, with user access control.
     * Ensures that users can only access their own events.
     * 
     * @param eventId The ID of the event to retrieve
     * @param userId The ID of the user requesting the event
     * @return The event entity
     * @throws ResourceNotFoundException if the event doesn't exist
     * @throws EventException if the user doesn't have permission to access the event
     */
    @Transactional(readOnly = true)
    public Event getEventById(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + eventId));

        if (!event.getUser().getId().equals(userId)) {
            throw new EventException("No tienes permiso para acceder a este evento");
        }

        return event;
    }

    /**
     * Retrieves a specific event by its ID for administrative purposes.
     * Bypasses the user access control check.
     * 
     * @param eventId The ID of the event to retrieve
     * @return The event entity
     * @throws ResourceNotFoundException if the event doesn't exist
     */
    @Transactional(readOnly = true)
    public Event getEventByIdForAdmin(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + eventId));
    }

    /**
     * Creates a new event for a specific user.
     * Validates the event times before creation.
     * 
     * @param eventCreateDTO Data transfer object containing the event details
     * @param userId The ID of the user who owns the event
     * @return The created event entity
     * @throws ResourceNotFoundException if the user doesn't exist
     * @throws EventException if the event times are invalid
     */
    @Transactional
    public Event createEvent(EventCreateDTO eventCreateDTO, Long userId) {
        validateEventTimes(eventCreateDTO.getStartTime(), eventCreateDTO.getEndTime());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        Event event = EventMapper.toEntity(eventCreateDTO, user);
        return eventRepository.save(event);
    }

    /**
     * Updates an existing event for a specific user.
     * Validates the event times and ensures the user has permission to update the event.
     * 
     * @param eventUpdateDTO Data transfer object containing the updated event details
     * @param userId The ID of the user who owns the event
     * @return The updated event entity
     * @throws ResourceNotFoundException if the event doesn't exist
     * @throws EventException if the user doesn't have permission or the event times are invalid
     */
    @Transactional
    public Event updateEvent(EventUpdateDTO eventUpdateDTO, Long userId) {
        Event existingEvent = getEventById(eventUpdateDTO.getId(), userId);

        LocalDateTime startTime = eventUpdateDTO.getStartTime() != null ?
                eventUpdateDTO.getStartTime() : existingEvent.getStartTime();
        LocalDateTime endTime = eventUpdateDTO.getEndTime() != null ?
                eventUpdateDTO.getEndTime() : existingEvent.getEndTime();

        validateEventTimes(startTime, endTime);

        EventMapper.updateEntityFromDTO(eventUpdateDTO, existingEvent);

        return eventRepository.save(existingEvent);
    }

    /**
     * Updates an existing event for administrative purposes.
     * Bypasses the user permission check but still validates event times.
     * 
     * @param eventUpdateDTO Data transfer object containing the updated event details
     * @return The updated event entity
     * @throws ResourceNotFoundException if the event doesn't exist
     * @throws EventException if the event times are invalid
     */
    @Transactional
    public Event updateEventForAdmin(EventUpdateDTO eventUpdateDTO) {
        Event existingEvent = getEventByIdForAdmin(eventUpdateDTO.getId());

        LocalDateTime startTime = eventUpdateDTO.getStartTime() != null ?
                eventUpdateDTO.getStartTime() : existingEvent.getStartTime();
        LocalDateTime endTime = eventUpdateDTO.getEndTime() != null ?
                eventUpdateDTO.getEndTime() : existingEvent.getEndTime();

        validateEventTimes(startTime, endTime);

        EventMapper.updateEntityFromDTO(eventUpdateDTO, existingEvent);

        return eventRepository.save(existingEvent);
    }

    /**
     * Deletes an event for a specific user.
     * Ensures the user has permission to delete the event.
     * 
     * @param eventId The ID of the event to delete
     * @param userId The ID of the user who owns the event
     * @throws ResourceNotFoundException if the event doesn't exist
     * @throws EventException if the user doesn't have permission to delete the event
     */
    @Transactional
    public void deleteEvent(Long eventId, Long userId) {
        Event event = getEventById(eventId, userId);

        eventRepository.delete(event);
    }

    /**
     * Deletes an event for administrative purposes.
     * Bypasses the user permission check.
     * 
     * @param eventId The ID of the event to delete
     * @throws ResourceNotFoundException if the event doesn't exist
     */
    @Transactional
    public void deleteEventForAdmin(Long eventId) {
        Event event = getEventByIdForAdmin(eventId);

        eventRepository.delete(event);
    }


    /**
     * Searches for events by title for a specific user.
     * 
     * @param userId The ID of the user whose events to search
     * @param title The title or partial title to search for
     * @return A list of matching events
     * @throws EventException if the search title is empty
     */
    @Transactional(readOnly = true)
    public List<Event> findEventsByTitle(Long userId, String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new EventException("El título de búsqueda no puede estar vacío");
        }

        return eventRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title);
    }

    /**
     * Searches for events by title across all users.
     * Typically used by administrators.
     * 
     * @param title The title or partial title to search for
     * @return A list of matching events from all users
     * @throws EventException if the search title is empty
     */
    @Transactional(readOnly = true)
    public List<Event> findAllEventsByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new EventException("El título de búsqueda no puede estar vacío");
        }

        return eventRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Validates that event start and end times are properly set and logically consistent.
     * 
     * @param startTime The event start time
     * @param endTime The event end time
     * @throws EventException if times are null or if start time is not before end time
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
