package com.blinky.apillama3blinky.controller;

import com.blinky.apillama3blinky.controller.dto.EventCreateDTO;
import com.blinky.apillama3blinky.controller.dto.EventDTO;
import com.blinky.apillama3blinky.controller.dto.EventUpdateDTO;
import com.blinky.apillama3blinky.exception.EventException;
import com.blinky.apillama3blinky.mapping.EventMapper;
import com.blinky.apillama3blinky.model.Event;
import com.blinky.apillama3blinky.security.JwtUtil;
import com.blinky.apillama3blinky.service.EventService;
import com.blinky.apillama3blinky.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public EventController(EventService eventService, UserService userService, JwtUtil jwtUtil) {
        this.eventService = eventService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        boolean isAdmin = isAdminFromRequest(request);

        List<Event> events;
        if (isAdmin) {
            // Admin can see all events
            events = eventService.getAllEvents();
        } else {
            // Non-admin can only see their own events
            events = eventService.getAllEventsByUserId(userId);
        }

        return ResponseEntity.ok(EventMapper.toDTOList(events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        boolean isAdmin = isAdminFromRequest(request);

        // If user is admin, they can access any event
        // Otherwise, they can only access their own events
        Event event;
        if (isAdmin) {
            event = eventService.getEventByIdForAdmin(id);
        } else {
            event = eventService.getEventById(id, userId);
        }

        return ResponseEntity.ok(EventMapper.toDTO(event));
    }

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventCreateDTO eventCreateDTO,
                                                HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);

        Event createdEvent = eventService.createEvent(eventCreateDTO, userId);
        return new ResponseEntity<>(EventMapper.toDTO(createdEvent), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(@PathVariable Long id,
                                                @Valid @RequestBody EventUpdateDTO eventUpdateDTO,
                                                HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        boolean isAdmin = isAdminFromRequest(request);

        // Set the id from the path variable
        eventUpdateDTO.setId(id);

        Event updatedEvent;
        if (isAdmin) {
            // Admin can update any event
            updatedEvent = eventService.updateEventForAdmin(eventUpdateDTO);
        } else {
            // Non-admin can only update their own events
            updatedEvent = eventService.updateEvent(eventUpdateDTO, userId);
        }

        return ResponseEntity.ok(EventMapper.toDTO(updatedEvent));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        boolean isAdmin = isAdminFromRequest(request);

        if (isAdmin) {
            // Admin can delete any event
            eventService.deleteEventForAdmin(id);
        } else {
            // Non-admin can only delete their own events
            eventService.deleteEvent(id, userId);
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventDTO>> getEventsByTitle(
            @RequestParam String title,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        boolean isAdmin = isAdminFromRequest(request);

        List<Event> events;
        if (isAdmin) {
            // Admin can search all events
            events = eventService.findAllEventsByTitle(title);
        } else {
            // Non-admin can only search their own events
            events = eventService.findEventsByTitle(userId, title);
        }

        return ResponseEntity.ok(EventMapper.toDTOList(events));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EventDTO>> getEventsByUserId(@PathVariable Long userId, HttpServletRequest request) {
        Long requestingUserId = getUserIdFromRequest(request);
        boolean isAdmin = isAdminFromRequest(request);

        // If user is not admin and trying to access another user's events, throw exception
        if (!isAdmin && !requestingUserId.equals(userId)) {
            throw new EventException("No tienes permiso para acceder a los eventos de otro usuario");
        }

        List<Event> events = eventService.getAllEventsByUserId(userId);
        return ResponseEntity.ok(EventMapper.toDTOList(events));
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new EventException("Se requiere token de autorización");
        }

        String token = authHeader.substring(7);

        // Extract userId directly from token
        Long userId = jwtUtil.extractUserId(token);

        if (userId == null) {
            // Fallback to extracting by email if userId is not in token
            String email = jwtUtil.extractUsername(token);
            if (email == null) {
                throw new EventException("Token inválido");
            }
            return userService.getUserByEmail(email).getId();
        }

        return userId;
    }

    private boolean isAdminFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.substring(7);
        Boolean isAdmin = jwtUtil.extractIsAdmin(token);

        return isAdmin != null && isAdmin;
    }
}
