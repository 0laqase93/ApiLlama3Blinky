package com.blinky.apillama3blinky.security;

import com.blinky.apillama3blinky.controller.dto.PromptDTO;
import com.blinky.apillama3blinky.exception.EventException;
import com.blinky.apillama3blinky.exception.ResourceNotFoundException;
import com.blinky.apillama3blinky.model.Event;
import com.blinky.apillama3blinky.model.User;
import com.blinky.apillama3blinky.service.EventService;
import com.blinky.apillama3blinky.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for JWT token operations and authentication.
 * Handles token generation, validation, extraction of claims, and user authorization.
 * Also provides methods for event access control based on user roles.
 */
@Component
public class JwtUtil {

    // JWT configuration properties
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private final UserService userService;
    private final EventService eventService;

    @Autowired
    public JwtUtil(UserService userService, EventService eventService) {
        this.userService = userService;
        this.eventService = eventService;
    }

    /**
     * Extracts the username (subject) from a JWT token.
     * 
     * @param token The JWT token
     * @return The username stored in the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a JWT token.
     * 
     * @param token The JWT token
     * @return The expiration date of the token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts the admin status from a JWT token.
     * 
     * @param token The JWT token
     * @return Boolean indicating if the user is an admin
     */
    public Boolean extractIsAdmin(String token) {
        return extractClaim(token, claims -> claims.get("isAdmin", Boolean.class));
    }

    /**
     * Extracts the user ID from a JWT token.
     * 
     * @param token The JWT token
     * @return The user ID stored in the token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Generic method to extract a specific claim from a JWT token.
     * 
     * @param token The JWT token
     * @param claimsResolver Function to extract the desired claim
     * @return The extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a JWT token.
     * 
     * @param token The JWT token
     * @return All claims contained in the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    /**
     * Checks if a JWT token has expired.
     * 
     * @param token The JWT token
     * @return True if the token has expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generates a JWT token for a user with additional claims.
     * 
     * @param userDetails The user details from Spring Security
     * @param isAdmin Boolean indicating if the user is an admin
     * @param userId The ID of the user
     * @return A JWT token string
     */
    public String generateToken(UserDetails userDetails, boolean isAdmin, Long userId) {
        // Create a map of claims to include in the token
        Map<String, Object> claims = new HashMap<>();
        claims.put("isAdmin", isAdmin);
        claims.put("userId", userId);
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Creates a JWT token with the specified claims and subject.
     * 
     * @param claims Map of claims to include in the token
     * @param subject The subject of the token (typically the username)
     * @return A JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * Validates a JWT token for a specific user.
     * 
     * @param token The JWT token to validate
     * @param userDetails The user details to validate against
     * @return True if the token is valid for the user, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Extracts the JWT token from an HTTP request.
     * 
     * @param request The HTTP request containing the Authorization header
     * @return The JWT token string
     * @throws EventException if the Authorization header is missing or invalid
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new EventException("Se requiere token de autorización");
        }

        // Extract the token part (remove "Bearer " prefix)
        return authHeader.substring(7);
    }

    /**
     * Extracts the JWT token from an HTTP request for authentication purposes.
     * Similar to getTokenFromRequest but throws a different exception type.
     * 
     * @param request The HTTP request containing the Authorization header
     * @return The JWT token string
     * @throws IllegalArgumentException if the Authorization header is missing or invalid
     */
    public String getTokenFromRequestForAuth(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Se requiere token de autorización");
        }

        // Extract the token part (remove "Bearer " prefix)
        return authHeader.substring(7);
    }

    /**
     * Extracts the user ID from the JWT token in an HTTP request.
     * If the user ID is not in the token, attempts to find it by email.
     * 
     * @param request The HTTP request containing the Authorization header
     * @return The user ID
     * @throws EventException if the token is invalid or the user cannot be found
     */
    public Long getUserIdFromRequest(HttpServletRequest request) {
        // Extract the token from the request
        String token = getTokenFromRequest(request);

        // Try to get the user ID directly from the token
        Long userId = extractUserId(token);

        // If user ID is not in the token, try to find the user by email
        if (userId == null) {
            String email = extractUsername(token);
            if (email == null) {
                throw new EventException("Token inválido");
            }
            return userService.getUserByEmail(email).getId();
        }

        return userId;
    }

    /**
     * Checks if the user in the HTTP request is an admin.
     * 
     * @param request The HTTP request containing the Authorization header
     * @return True if the user is an admin, false otherwise
     */
    public boolean isAdminFromRequest(HttpServletRequest request) {
        try {
            // Extract the token and check the admin status
            String token = getTokenFromRequest(request);
            Boolean isAdmin = extractIsAdmin(token);

            return isAdmin != null && isAdmin;
        } catch (EventException e) {
            // If there's an issue with the token, assume the user is not an admin
            return false;
        }
    }

    /**
     * Safely extracts the JWT token from an HTTP request without throwing exceptions.
     * 
     * @param request The HTTP request containing the Authorization header
     * @return The JWT token string, or null if the header is missing or invalid
     */
    public String extractTokenSafely(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    public User resetPasswordFromToken(String token, String newPassword) {
        String email = extractUsername(token);
        if (email == null) {
            throw new IllegalArgumentException("Token inválido");
        }

        // Reset the password
        return userService.resetPassword(email, newPassword);
    }

    public User resetPasswordFromRequest(HttpServletRequest request, String newPassword) {
        String token = getTokenFromRequestForAuth(request);

        return resetPasswordFromToken(token, newPassword);
    }

    public boolean verifyPasswordFromToken(String token, String password) {
        String email = extractUsername(token);
        if (email == null) {
            throw new IllegalArgumentException("Token inválido");
        }

        User user = userService.getUserByEmail(email);

        return user.getPassword().equals(password);
    }

    public boolean verifyPasswordFromRequest(HttpServletRequest request, String password) {
        String token = getTokenFromRequestForAuth(request);

        return verifyPasswordFromToken(token, password);
    }

    public ResponseEntity<Boolean> resetPasswordWithResponse(HttpServletRequest request, String newPassword) {
        try {
            resetPasswordFromRequest(request, newPassword);

            return ResponseEntity.ok(true);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    public ResponseEntity<Boolean> verifyPasswordWithResponse(HttpServletRequest request, String password) {
        try {
            boolean isPasswordValid = verifyPasswordFromRequest(request, password);

            return ResponseEntity.ok(isPasswordValid);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    public List<Event> getAllEventsForUser(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        boolean isAdmin = isAdminFromRequest(request);

        if (isAdmin) {
            return eventService.getAllEvents();
        } else {
            return eventService.getAllEventsByUserId(userId);
        }
    }

    public Event getEventByIdForUser(HttpServletRequest request, Long eventId) {
        Long userId = getUserIdFromRequest(request);
        boolean isAdmin = isAdminFromRequest(request);

        if (isAdmin) {
            return eventService.getEventByIdForAdmin(eventId);
        } else {
            return eventService.getEventById(eventId, userId);
        }
    }

    public Event updateEventForUser(HttpServletRequest request, com.blinky.apillama3blinky.controller.dto.EventUpdateDTO eventUpdateDTO) {
        Long userId = getUserIdFromRequest(request);
        boolean isAdmin = isAdminFromRequest(request);

        if (isAdmin) {
            return eventService.updateEventForAdmin(eventUpdateDTO);
        } else {
            return eventService.updateEvent(eventUpdateDTO, userId);
        }
    }

    public void deleteEventForUser(HttpServletRequest request, Long eventId) {
        Long userId = getUserIdFromRequest(request);
        boolean isAdmin = isAdminFromRequest(request);

        if (isAdmin) {
            eventService.deleteEventForAdmin(eventId);
        } else {
            eventService.deleteEvent(eventId, userId);
        }
    }

    public List<Event> getEventsByTitleForUser(HttpServletRequest request, String title) {
        Long userId = getUserIdFromRequest(request);
        boolean isAdmin = isAdminFromRequest(request);

        if (isAdmin) {
            return eventService.findAllEventsByTitle(title);
        } else {
            return eventService.findEventsByTitle(userId, title);
        }
    }

    /**
     * Checks if the user in the request has permission to access another user's events.
     * Only admins or the user themselves can access a user's events.
     * 
     * @param request The HTTP request containing the JWT token
     * @param targetUserId The ID of the user whose events are being accessed
     * @return True if access is allowed
     * @throws EventException if access is denied
     */
    public boolean checkUserEventAccessPermission(HttpServletRequest request, Long targetUserId) {
        Long requestingUserId = getUserIdFromRequest(request);
        boolean isAdmin = isAdminFromRequest(request);

        // Only admins or the user themselves can access a user's events
        if (!isAdmin && !requestingUserId.equals(targetUserId)) {
            throw new EventException("No tienes permiso para acceder a los eventos de otro usuario");
        }

        return true;
    }


    /**
     * Creates an event for the user in the request.
     * 
     * @param request The HTTP request containing the JWT token
     * @param eventCreateDTO Data transfer object containing the event details
     * @return The created event entity
     */
    public Event createEventForUser(HttpServletRequest request, com.blinky.apillama3blinky.controller.dto.EventCreateDTO eventCreateDTO) {
        // Extract the user ID from the request
        Long userId = getUserIdFromRequest(request);
        // Delegate to the event service to create the event
        return eventService.createEvent(eventCreateDTO, userId);
    }
}
