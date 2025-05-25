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

    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents(HttpServletRequest request) {
        List<Event> events = jwtUtil.getAllEventsForUser(request);
        return ResponseEntity.ok(EventMapper.toDTOList(events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id, HttpServletRequest request) {
        Event event = jwtUtil.getEventByIdForUser(request, id);
        return ResponseEntity.ok(EventMapper.toDTO(event));
    }

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventCreateDTO eventCreateDTO,
                                                HttpServletRequest request) {
        Event createdEvent = jwtUtil.createEventForUser(request, eventCreateDTO);
        return new ResponseEntity<>(EventMapper.toDTO(createdEvent), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(@PathVariable Long id,
                                                @Valid @RequestBody EventUpdateDTO eventUpdateDTO,
                                                HttpServletRequest request) {
        eventUpdateDTO.setId(id);
        Event updatedEvent = jwtUtil.updateEventForUser(request, eventUpdateDTO);
        return ResponseEntity.ok(EventMapper.toDTO(updatedEvent));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, HttpServletRequest request) {
        jwtUtil.deleteEventForUser(request, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventDTO>> getEventsByTitle(
            @RequestParam String title,
            HttpServletRequest request) {
        List<Event> events = jwtUtil.getEventsByTitleForUser(request, title);
        return ResponseEntity.ok(EventMapper.toDTOList(events));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EventDTO>> getEventsByUserId(@PathVariable Long userId, HttpServletRequest request) {
        jwtUtil.checkUserEventAccessPermission(request, userId);
        List<Event> events = eventService.getAllEventsByUserId(userId);
        return ResponseEntity.ok(EventMapper.toDTOList(events));
    }
}
