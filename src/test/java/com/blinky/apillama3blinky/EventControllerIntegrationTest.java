package com.blinky.apillama3blinky;

import com.blinky.apillama3blinky.controller.dto.EventCreateDTO;
import com.blinky.apillama3blinky.controller.dto.EventDTO;
import com.blinky.apillama3blinky.controller.dto.EventUpdateDTO;
import com.blinky.apillama3blinky.model.Event;
import com.blinky.apillama3blinky.model.User;
import com.blinky.apillama3blinky.repository.EventRepository;
import com.blinky.apillama3blinky.repository.UserRepository;
import com.blinky.apillama3blinky.security.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for the EventController.
 * 
 * This test class provides complete test coverage for all EventController endpoints:
 * - GET /api/events (list all events)
 * - GET /api/events/{id} (get a specific event)
 * - POST /api/events (create an event)
 * - PUT /api/events/{id} (update an event)
 * - DELETE /api/events/{id} (delete an event)
 * - GET /api/events/search?title={title} (search events by title)
 * - GET /api/events/user/{userId} (get events by user ID)
 * 
 * Testing strategy:
 * 1. Tests both positive and negative scenarios for each endpoint
 * 2. Tests authorization rules (regular users vs admin users)
 * 3. Tests business rules (e.g., users can only access their own events)
 * 4. Tests validation rules (e.g., invalid data, non-existent resources)
 * 
 * Test setup:
 * - Uses H2 in-memory database for testing
 * - Creates three test users: regular user, admin user, and another regular user
 * - Creates test events for each user
 * - Generates proper JWT tokens with appropriate roles and user IDs
 * - Cleans up all test data after each test
 * 
 * Each test method follows a consistent pattern:
 * 1. Arrange: Set up test data and request parameters
 * 2. Act: Send the HTTP request to the endpoint
 * 3. Assert: Verify the response status, body, and any side effects
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class EventControllerIntegrationTest {

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
    private User regularUser;
    private User adminUser;
    private User anotherUser;
    private String regularUserToken;
    private String adminUserToken;
    private String anotherUserToken;
    private HttpHeaders regularUserHeaders;
    private HttpHeaders adminUserHeaders;
    private HttpHeaders anotherUserHeaders;
    private List<Event> regularUserEvents;
    private List<Event> adminUserEvents;
    private List<Event> anotherUserEvents;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/api/events";

        // Clean up any existing events and users
        eventRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        regularUser = createUser("regular@example.com", "Regular User", false);
        adminUser = createUser("admin@example.com", "Admin User", true);
        anotherUser = createUser("another@example.com", "Another User", false);

        // Generate JWT tokens
        regularUserToken = generateToken(regularUser, false);
        adminUserToken = generateToken(adminUser, true);
        anotherUserToken = generateToken(anotherUser, false);

        // Set up headers
        regularUserHeaders = createHeaders(regularUserToken);
        adminUserHeaders = createHeaders(adminUserToken);
        anotherUserHeaders = createHeaders(anotherUserToken);

        // Create test events
        regularUserEvents = createTestEventsForUser(regularUser, "Regular User Event", 3);
        adminUserEvents = createTestEventsForUser(adminUser, "Admin User Event", 3);
        anotherUserEvents = createTestEventsForUser(anotherUser, "Another User Event", 3);
    }

    @AfterEach
    public void tearDown() {
        // Clean up all test data
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========== GET /api/events (Get All Events) Tests ==========

    @Test
    public void testGetAllEvents_AsRegularUser_ShouldReturnOnlyOwnEvents() {
        // Send request as regular user
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<List<EventDTO>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<EventDTO>>() {}
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<EventDTO> events = response.getBody();
        assertNotNull(events);
        assertEquals(regularUserEvents.size(), events.size());

        // Verify all events belong to the regular user
        for (EventDTO event : events) {
            assertEquals(regularUser.getId(), event.getUserId());
        }
    }

    @Test
    public void testGetAllEvents_AsAdminUser_ShouldReturnAllEvents() {
        // Send request as admin user
        HttpEntity<?> request = new HttpEntity<>(adminUserHeaders);
        ResponseEntity<List<EventDTO>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<EventDTO>>() {}
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<EventDTO> events = response.getBody();
        assertNotNull(events);

        // Admin should see all events (regular + admin + another)
        int totalEvents = regularUserEvents.size() + adminUserEvents.size() + anotherUserEvents.size();
        assertEquals(totalEvents, events.size());
    }

    @Test
    public void testGetAllEvents_WithoutAuth_ShouldReturnForbidden() {
        // Send request without auth token
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class);

        // Verify response
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // ========== GET /api/events/{id} (Get Event by ID) Tests ==========

    @Test
    public void testGetEventById_AsRegularUser_OwnEvent_ShouldSucceed() {
        // Get an event created by the regular user
        Event event = regularUserEvents.get(0);

        // Send request as regular user
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<EventDTO> response = restTemplate.exchange(
                baseUrl + "/" + event.getId(),
                HttpMethod.GET,
                request,
                EventDTO.class
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        EventDTO eventDTO = response.getBody();
        assertNotNull(eventDTO);
        assertEquals(event.getId(), eventDTO.getId());
        assertEquals(event.getTitle(), eventDTO.getTitle());
        assertEquals(regularUser.getId(), eventDTO.getUserId());
    }

    @Test
    public void testGetEventById_AsRegularUser_OtherUserEvent_ShouldFail() {
        // Get an event created by another user
        Event event = anotherUserEvents.get(0);

        // Send request as regular user
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + event.getId(),
                HttpMethod.GET,
                request,
                String.class
        );

        // Verify response (should be forbidden or not found)
        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    public void testGetEventById_AsAdminUser_OtherUserEvent_ShouldSucceed() {
        // Get an event created by the regular user
        Event event = regularUserEvents.get(0);

        // Send request as admin user
        HttpEntity<?> request = new HttpEntity<>(adminUserHeaders);
        ResponseEntity<EventDTO> response = restTemplate.exchange(
                baseUrl + "/" + event.getId(),
                HttpMethod.GET,
                request,
                EventDTO.class
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        EventDTO eventDTO = response.getBody();
        assertNotNull(eventDTO);
        assertEquals(event.getId(), eventDTO.getId());
        assertEquals(event.getTitle(), eventDTO.getTitle());
        assertEquals(regularUser.getId(), eventDTO.getUserId());
    }

    @Test
    public void testGetEventById_NonExistentId_ShouldReturnNotFound() {
        // Use a non-existent event ID
        Long nonExistentId = 999999L;

        // Send request as admin user (who should have access to all events)
        HttpEntity<?> request = new HttpEntity<>(adminUserHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + nonExistentId,
                HttpMethod.GET,
                request,
                String.class
        );

        // Verify response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ========== POST /api/events (Create Event) Tests ==========

    @Test
    public void testCreateEvent_AsRegularUser_ShouldSucceed() {
        // Create event DTO
        EventCreateDTO eventCreateDTO = new EventCreateDTO(
                "New Test Event",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                "Test Location",
                "Test Description"
        );

        // Send request as regular user
        HttpEntity<EventCreateDTO> request = new HttpEntity<>(eventCreateDTO, regularUserHeaders);
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
        assertEquals("New Test Event", createdEvent.getTitle());
        assertEquals("Test Location", createdEvent.getLocation());
        assertEquals("Test Description", createdEvent.getDescription());
        assertEquals(regularUser.getId(), createdEvent.getUserId());

        // Verify event was saved to database
        assertTrue(eventRepository.existsById(createdEvent.getId()));
    }

    @Test
    public void testCreateEvent_WithInvalidData_ShouldReturnBadRequest() {
        // Create event DTO with invalid data (null title)
        EventCreateDTO eventCreateDTO = new EventCreateDTO(
                null, // Invalid: title is null
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                "Test Location",
                "Test Description"
        );

        // Send request as regular user
        HttpEntity<EventCreateDTO> request = new HttpEntity<>(eventCreateDTO, regularUserHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testCreateEvent_WithEndTimeBeforeStartTime_ShouldReturnBadRequest() {
        // Create event DTO with invalid times (end before start)
        EventCreateDTO eventCreateDTO = new EventCreateDTO(
                "Invalid Time Event",
                LocalDateTime.now().plusHours(2), // Start time is after end time
                LocalDateTime.now().plusHours(1), // End time is before start time
                "Test Location",
                "Test Description"
        );

        // Send request as regular user
        HttpEntity<EventCreateDTO> request = new HttpEntity<>(eventCreateDTO, regularUserHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== PUT /api/events/{id} (Update Event) Tests ==========

    @Test
    public void testUpdateEvent_AsRegularUser_OwnEvent_ShouldSucceed() {
        // Get an event created by the regular user
        Event event = regularUserEvents.get(0);

        // Create update DTO
        EventUpdateDTO updateDTO = new EventUpdateDTO(
                "Updated Event Title",
                event.getStartTime(),
                event.getEndTime(),
                "Updated Location",
                "Updated Description"
        );

        // Send request as regular user
        HttpEntity<EventUpdateDTO> request = new HttpEntity<>(updateDTO, regularUserHeaders);
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
        assertEquals("Updated Event Title", updatedEvent.getTitle());
        assertEquals("Updated Location", updatedEvent.getLocation());
        assertEquals("Updated Description", updatedEvent.getDescription());
        assertEquals(regularUser.getId(), updatedEvent.getUserId());
    }

    @Test
    public void testUpdateEvent_AsRegularUser_OtherUserEvent_ShouldFail() {
        // Get an event created by another user
        Event event = anotherUserEvents.get(0);

        // Create update DTO
        EventUpdateDTO updateDTO = new EventUpdateDTO(
                "Unauthorized Update",
                event.getStartTime(),
                event.getEndTime(),
                "Unauthorized Location",
                "Unauthorized Description"
        );

        // Send request as regular user
        HttpEntity<EventUpdateDTO> request = new HttpEntity<>(updateDTO, regularUserHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + event.getId(),
                HttpMethod.PUT,
                request,
                String.class
        );

        // Verify response (should be forbidden or not found)
        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    public void testUpdateEvent_AsAdminUser_OtherUserEvent_ShouldSucceed() {
        // Get an event created by the regular user
        Event event = regularUserEvents.get(0);

        // Create update DTO
        EventUpdateDTO updateDTO = new EventUpdateDTO(
                "Admin Updated Event",
                event.getStartTime(),
                event.getEndTime(),
                "Admin Updated Location",
                "Admin Updated Description"
        );

        // Send request as admin user
        HttpEntity<EventUpdateDTO> request = new HttpEntity<>(updateDTO, adminUserHeaders);
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
        assertEquals("Admin Updated Event", updatedEvent.getTitle());
        assertEquals("Admin Updated Location", updatedEvent.getLocation());
        assertEquals("Admin Updated Description", updatedEvent.getDescription());
        assertEquals(regularUser.getId(), updatedEvent.getUserId()); // User ID should not change
    }

    // ========== DELETE /api/events/{id} (Delete Event) Tests ==========

    @Test
    public void testDeleteEvent_AsRegularUser_OwnEvent_ShouldSucceed() {
        // Get an event created by the regular user
        Event event = regularUserEvents.get(0);

        // Send request as regular user
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
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
    public void testDeleteEvent_AsRegularUser_OtherUserEvent_ShouldFail() {
        // Get an event created by another user
        Event event = anotherUserEvents.get(0);

        // Send request as regular user
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + event.getId(),
                HttpMethod.DELETE,
                request,
                String.class
        );

        // Verify response (should be forbidden or not found)
        assertTrue(response.getStatusCode().is4xxClientError());

        // Verify event is NOT deleted
        assertTrue(eventRepository.existsById(event.getId()));
    }

    @Test
    public void testDeleteEvent_AsAdminUser_OtherUserEvent_ShouldSucceed() {
        // Get an event created by the regular user
        Event event = regularUserEvents.get(0);

        // Send request as admin user
        HttpEntity<?> request = new HttpEntity<>(adminUserHeaders);
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

    // ========== GET /api/events/search (Search Events) Tests ==========

    @Test
    public void testSearchEvents_AsRegularUser_ShouldReturnOnlyOwnEvents() {
        // Create some events with specific titles for searching
        createTestEvent(regularUser, "Searchable Regular Event", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        createTestEvent(adminUser, "Searchable Admin Event", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        createTestEvent(anotherUser, "Searchable Another Event", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));

        // Send request as regular user
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<List<EventDTO>> response = restTemplate.exchange(
                baseUrl + "/search?title=Searchable",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<EventDTO>>() {}
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<EventDTO> events = response.getBody();
        assertNotNull(events);

        // Should only return the regular user's searchable event
        assertEquals(1, events.size());
        assertEquals(regularUser.getId(), events.get(0).getUserId());
        assertTrue(events.get(0).getTitle().contains("Searchable"));
    }

    @Test
    public void testSearchEvents_AsAdminUser_ShouldReturnAllMatchingEvents() {
        // Create some events with specific titles for searching
        createTestEvent(regularUser, "Searchable Regular Event", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        createTestEvent(adminUser, "Searchable Admin Event", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        createTestEvent(anotherUser, "Searchable Another Event", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));

        // Send request as admin user
        HttpEntity<?> request = new HttpEntity<>(adminUserHeaders);
        ResponseEntity<List<EventDTO>> response = restTemplate.exchange(
                baseUrl + "/search?title=Searchable",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<EventDTO>>() {}
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<EventDTO> events = response.getBody();
        assertNotNull(events);

        // Should return all searchable events (3)
        assertEquals(3, events.size());
        assertTrue(events.stream().allMatch(e -> e.getTitle().contains("Searchable")));
    }

    @Test
    public void testSearchEvents_WithNoResults_ShouldReturnEmptyList() {
        // Send request with a search term that won't match any events
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<List<EventDTO>> response = restTemplate.exchange(
                baseUrl + "/search?title=NonExistentTitle",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<EventDTO>>() {}
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<EventDTO> events = response.getBody();
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    // ========== GET /api/events/user/{userId} (Get Events by User ID) Tests ==========

    @Test
    public void testGetEventsByUserId_AsRegularUser_OwnEvents_ShouldSucceed() {
        // Send request as regular user for own events
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<List<EventDTO>> response = restTemplate.exchange(
                baseUrl + "/user/" + regularUser.getId(),
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<EventDTO>>() {}
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<EventDTO> events = response.getBody();
        assertNotNull(events);
        assertEquals(regularUserEvents.size(), events.size());

        // Verify all events belong to the regular user
        for (EventDTO event : events) {
            assertEquals(regularUser.getId(), event.getUserId());
        }
    }

    @Test
    public void testGetEventsByUserId_AsRegularUser_OtherUserEvents_ShouldFail() {
        // Send request as regular user for another user's events
        HttpEntity<?> request = new HttpEntity<>(regularUserHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/user/" + anotherUser.getId(),
                HttpMethod.GET,
                request,
                String.class
        );

        // Verify response (should be forbidden or bad request)
        assertTrue(response.getStatusCode().is4xxClientError());
        assertTrue(response.getBody().contains("No tienes permiso"));
    }

    @Test
    public void testGetEventsByUserId_AsAdminUser_OtherUserEvents_ShouldSucceed() {
        // Send request as admin user for regular user's events
        HttpEntity<?> request = new HttpEntity<>(adminUserHeaders);
        ResponseEntity<List<EventDTO>> response = restTemplate.exchange(
                baseUrl + "/user/" + regularUser.getId(),
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<EventDTO>>() {}
        );

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<EventDTO> events = response.getBody();
        assertNotNull(events);
        assertEquals(regularUserEvents.size(), events.size());

        // Verify all events belong to the regular user
        for (EventDTO event : events) {
            assertEquals(regularUser.getId(), event.getUserId());
        }
    }

    // ========== Helper Methods ==========

    private User createUser(String email, String username, boolean isAdmin) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setUsername(username);
        user.setAdmin(isAdmin);
        return userRepository.save(user);
    }

    private String generateToken(User user, boolean isAdmin) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (isAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );

        return jwtUtil.generateToken(userDetails, user.isAdmin(), user.getId());
    }

    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private List<Event> createTestEventsForUser(User user, String titlePrefix, int count) {
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Event event = createTestEvent(user, titlePrefix + " " + (i + 1), 
                    LocalDateTime.now().plusDays(i + 1), 
                    LocalDateTime.now().plusDays(i + 1).plusHours(2));
            events.add(event);
        }
        return events;
    }

    private Event createTestEvent(User user, String title, LocalDateTime startTime, LocalDateTime endTime) {
        Event event = new Event(title, startTime, endTime, user);
        event.setLocation("Test Location for " + title);
        event.setDescription("Test Description for " + title);
        return eventRepository.save(event);
    }
}
