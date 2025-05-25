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

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Autowired
    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Event> getAllEventsByUserId(Long userId) {
        return eventRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Event getEventById(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + eventId));

        if (!event.getUser().getId().equals(userId)) {
            throw new EventException("No tienes permiso para acceder a este evento");
        }

        return event;
    }

    @Transactional(readOnly = true)
    public Event getEventByIdForAdmin(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + eventId));
    }

    @Transactional
    public Event createEvent(EventCreateDTO eventCreateDTO, Long userId) {
        validateEventTimes(eventCreateDTO.getStartTime(), eventCreateDTO.getEndTime());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        Event event = EventMapper.toEntity(eventCreateDTO, user);
        return eventRepository.save(event);
    }

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

    @Transactional
    public void deleteEvent(Long eventId, Long userId) {
        Event event = getEventById(eventId, userId);

        eventRepository.delete(event);
    }

    @Transactional
    public void deleteEventForAdmin(Long eventId) {
        Event event = getEventByIdForAdmin(eventId);

        eventRepository.delete(event);
    }


    @Transactional(readOnly = true)
    public List<Event> findEventsByTitle(Long userId, String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new EventException("El título de búsqueda no puede estar vacío");
        }

        return eventRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title);
    }

    @Transactional(readOnly = true)
    public List<Event> findAllEventsByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new EventException("El título de búsqueda no puede estar vacío");
        }

        return eventRepository.findByTitleContainingIgnoreCase(title);
    }

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
