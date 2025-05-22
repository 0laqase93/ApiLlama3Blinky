package com.blinky.apillama3blinky;

import com.blinky.apillama3blinky.controller.dto.EventCreateDTO;
import com.blinky.apillama3blinky.controller.dto.EventDTO;
import com.blinky.apillama3blinky.controller.dto.EventUpdateDTO;
import com.blinky.apillama3blinky.model.Event;
import com.blinky.apillama3blinky.model.User;
import com.blinky.apillama3blinky.repository.EventRepository;
import com.blinky.apillama3blinky.repository.UserRepository;
import com.blinky.apillama3blinky.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class EventControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String baseUrl;
    private User testUser;
    private String authToken;
    private HttpHeaders headers;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/api/events";

        // Create test user if not exists
        testUser = userRepository.findByEmail("test@example.com")
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail("test@example.com");
                    user.setPassword("password");
                    user.setUsername("Test User");
                    return userRepository.save(user);
                });

        // Generate JWT token for the test user
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                testUser.getEmail(),
                testUser.getPassword(),
                java.util.Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")
                )
        );
        authToken = jwtUtil.generateToken(userDetails);

        // Set up headers with authentication
        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Clean up any existing events for the test user
        eventRepository.deleteAll(eventRepository.findByUserId(testUser.getId()));
    }

    @Test
    public void testCreateEvent() {
        // Create event DTO
        EventCreateDTO eventCreateDTO = new EventCreateDTO(
                "Test Event",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                "Test Location",
                "Test Description"
        );

        // Send request
        HttpEntity<EventCreateDTO> request = new HttpEntity<>(eventCreateDTO, headers);
        ResponseEntity<EventDTO> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                EventDTO.class
        );

        // Verify response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        EventDTO createdEvent = response.getBody();
        assertNotNull(createdEvent);
        assertEquals("Test Event", createdEvent.getTitle());
        assertEquals("Test Location", createdEvent.getLocation());
        assertEquals("Test Description", createdEvent.getDescription());
        assertEquals(testUser.getId(), createdEvent.getUserId());
    }

    @Test
    public void testGetAllEvents() {
        // Create some test events
        createTestEvent("Event 1");
        createTestEvent("Event 2");

        // Send request
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<EventDTO[]> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                request,
                EventDTO[].class
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        EventDTO[] events = response.getBody();
        assertNotNull(events);
        assertTrue(events.length >= 2);
    }

    @Test
    public void testGetEventById() {
        // Create a test event
        Event event = createTestEvent("Event for Get Test");

        // Send request
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<EventDTO> response = restTemplate.exchange(
                baseUrl + "/" + event.getId(),
                HttpMethod.GET,
                request,
                EventDTO.class
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        EventDTO retrievedEvent = response.getBody();
        assertNotNull(retrievedEvent);
        assertEquals(event.getId(), retrievedEvent.getId());
        assertEquals("Event for Get Test", retrievedEvent.getTitle());
    }

    @Test
    public void testUpdateEvent() {
        // Create a test event
        Event event = createTestEvent("Event for Update Test");

        // Create update DTO
        EventUpdateDTO updateDTO = new EventUpdateDTO(
                event.getId(),
                "Updated Event",
                event.getStartTime(),
                event.getEndTime(),
                "Updated Location",
                "Updated Description"
        );

        // Send request
        HttpEntity<EventUpdateDTO> request = new HttpEntity<>(updateDTO, headers);
        ResponseEntity<EventDTO> response = restTemplate.exchange(
                baseUrl + "/" + event.getId(),
                HttpMethod.PUT,
                request,
                EventDTO.class
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        EventDTO updatedEvent = response.getBody();
        assertNotNull(updatedEvent);
        assertEquals(event.getId(), updatedEvent.getId());
        assertEquals("Updated Event", updatedEvent.getTitle());
        assertEquals("Updated Location", updatedEvent.getLocation());
        assertEquals("Updated Description", updatedEvent.getDescription());
    }

    @Test
    public void testDeleteEvent() {
        // Create a test event
        Event event = createTestEvent("Event for Delete Test");

        // Send request
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + event.getId(),
                HttpMethod.DELETE,
                request,
                Void.class
        );

        // Verify response
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify event is deleted
        assertFalse(eventRepository.existsById(event.getId()));
    }

    @Test
    public void testGetEventsByUserId() {
        // Create some test events
        createTestEvent("Event 1 for User Test");
        createTestEvent("Event 2 for User Test");

        // Send request
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<EventDTO[]> response = restTemplate.exchange(
                baseUrl + "/user/" + testUser.getId(),
                HttpMethod.GET,
                request,
                EventDTO[].class
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        EventDTO[] events = response.getBody();
        assertNotNull(events);
        assertTrue(events.length >= 2);

        // Verify events belong to the test user
        for (EventDTO event : events) {
            assertEquals(testUser.getId(), event.getUserId());
        }
    }

    @Test
    public void testGetEventsByUserIdUnauthorized() {
        // Try to access events of a different user
        Long differentUserId = testUser.getId() + 1; // A different user ID

        // Send request
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/user/" + differentUserId,
                HttpMethod.GET,
                request,
                String.class
        );

        // Verify response (should be forbidden or contain error message)
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("No tienes permiso"));
    }


    // Helper method to create a test event
    private Event createTestEvent(String title) {
        return createTestEvent(title, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
    }

    private Event createTestEvent(String title, LocalDateTime startTime, LocalDateTime endTime) {
        Event event = new Event(title, startTime, endTime, testUser);
        event.setLocation("Test Location");
        event.setDescription("Test Description");
        return eventRepository.save(event);
    }
}
