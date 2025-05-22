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
        List<Event> events = eventService.getAllEventsByUserId(userId);
        return ResponseEntity.ok(EventMapper.toDTOList(events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Event event = eventService.getEventById(id, userId);
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

        if (!id.equals(eventUpdateDTO.getId())) {
            throw new EventException("El ID del evento en la URL no coincide con el ID en el cuerpo de la solicitud");
        }

        Event updatedEvent = eventService.updateEvent(eventUpdateDTO, userId);
        return ResponseEntity.ok(EventMapper.toDTO(updatedEvent));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        eventService.deleteEvent(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventDTO>> getEventsByTitle(
            @RequestParam String title,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<Event> events = eventService.findEventsByTitle(userId, title);
        return ResponseEntity.ok(EventMapper.toDTOList(events));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EventDTO>> getEventsByUserId(@PathVariable Long userId, HttpServletRequest request) {
        Long requestingUserId = getUserIdFromRequest(request);

        if (!requestingUserId.equals(userId)) {
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
        String email = jwtUtil.extractUsername(token);

        if (email == null) {
            throw new EventException("Token inválido");
        }

        return userService.getUserByEmail(email).getId();
    }
}
