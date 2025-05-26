package com.blinky.apillama3blinky.controller;

import com.blinky.apillama3blinky.controller.dto.EventCreateDTO;
import com.blinky.apillama3blinky.controller.dto.EventDTO;
import com.blinky.apillama3blinky.controller.dto.EventUpdateDTO;
import com.blinky.apillama3blinky.mapping.EventMapper;
import com.blinky.apillama3blinky.model.Event;
import com.blinky.apillama3blinky.security.JwtUtil;
import com.blinky.apillama3blinky.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for event management API endpoints.
 * Provides operations for creating, retrieving, updating, and deleting events,
 * with user-specific access controls.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final JwtUtil jwtUtil;

    @Autowired
    public EventController(EventService eventService, JwtUtil jwtUtil) {
        this.eventService = eventService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Retrieves all events for the authenticated user.
     * 
     * @param request The HTTP request containing the JWT token for user authentication
     * @return Response entity with a list of events belonging to the user
     */
    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents(HttpServletRequest request) {
        List<Event> events = jwtUtil.getAllEventsForUser(request);
        return ResponseEntity.ok(EventMapper.toDTOList(events));
    }

    /**
     * Retrieves a specific event by its ID for the authenticated user.
     * 
     * @param id The ID of the event to retrieve
     * @param request The HTTP request containing the JWT token for user authentication
     * @return Response entity with the requested event's details
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id, HttpServletRequest request) {
        Event event = jwtUtil.getEventByIdForUser(request, id);
        return ResponseEntity.ok(EventMapper.toDTO(event));
    }

    /**
     * Creates a new event for the authenticated user.
     * 
     * @param eventCreateDTO Data transfer object containing the new event's details
     * @param request The HTTP request containing the JWT token for user authentication
     * @return Response entity with the created event's details and HTTP 201 Created status
     */
    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventCreateDTO eventCreateDTO,
                                                HttpServletRequest request) {
        Event createdEvent = jwtUtil.createEventForUser(request, eventCreateDTO);
        return new ResponseEntity<>(EventMapper.toDTO(createdEvent), HttpStatus.CREATED);
    }

    /**
     * Updates an existing event for the authenticated user.
     * 
     * @param id The ID of the event to update
     * @param eventUpdateDTO Data transfer object containing the updated event details
     * @param request The HTTP request containing the JWT token for user authentication
     * @return Response entity with the updated event's details
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(@PathVariable Long id,
                                                @Valid @RequestBody EventUpdateDTO eventUpdateDTO,
                                                HttpServletRequest request) {
        eventUpdateDTO.setId(id);
        Event updatedEvent = jwtUtil.updateEventForUser(request, eventUpdateDTO);
        return ResponseEntity.ok(EventMapper.toDTO(updatedEvent));
    }

    /**
     * Deletes an event for the authenticated user.
     * 
     * @param id The ID of the event to delete
     * @param request The HTTP request containing the JWT token for user authentication
     * @return Response entity with HTTP 204 No Content status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, HttpServletRequest request) {
        jwtUtil.deleteEventForUser(request, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Searches for events by title for the authenticated user.
     * 
     * @param title The title or partial title to search for
     * @param request The HTTP request containing the JWT token for user authentication
     * @return Response entity with a list of matching events
     */
    @GetMapping("/search")
    public ResponseEntity<List<EventDTO>> getEventsByTitle(
            @RequestParam String title,
            HttpServletRequest request) {
        List<Event> events = jwtUtil.getEventsByTitleForUser(request, title);
        return ResponseEntity.ok(EventMapper.toDTOList(events));
    }

    /**
     * Retrieves all events for a specific user, with permission check.
     * 
     * @param userId The ID of the user whose events to retrieve
     * @param request The HTTP request containing the JWT token for user authentication
     * @return Response entity with a list of events belonging to the specified user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EventDTO>> getEventsByUserId(@PathVariable Long userId, HttpServletRequest request) {
        jwtUtil.checkUserEventAccessPermission(request, userId);
        List<Event> events = eventService.getAllEventsByUserId(userId);
        return ResponseEntity.ok(EventMapper.toDTOList(events));
    }
}
