package com.blinky.apillama3blinky.api;

import com.blinky.apillama3blinky.controller.dto.EventCreateDTO;
import com.blinky.apillama3blinky.controller.dto.EventDTO;
import com.blinky.apillama3blinky.controller.dto.EventUpdateDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface EventControllerApi {

    ResponseEntity<List<EventDTO>> getAllEvents(HttpServletRequest request);

    ResponseEntity<EventDTO> getEventById(
            @PathVariable Long id,
            HttpServletRequest request);

    ResponseEntity<EventDTO> createEvent(
            @Valid @RequestBody EventCreateDTO eventCreateDTO,
            HttpServletRequest request);

    ResponseEntity<EventDTO> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateDTO eventUpdateDTO,
            HttpServletRequest request);

    ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            HttpServletRequest request);

    ResponseEntity<List<EventDTO>> getEventsByTitle(
            @RequestParam String title,
            HttpServletRequest request);

    ResponseEntity<List<EventDTO>> getEventsByUserId(
            @PathVariable Long userId,
            HttpServletRequest request);
}