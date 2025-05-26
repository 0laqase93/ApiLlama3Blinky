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

@Component
public class JwtUtil {

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

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Boolean extractIsAdmin(String token) {
        return extractClaim(token, claims -> claims.get("isAdmin", Boolean.class));
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails, boolean isAdmin, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("isAdmin", isAdmin);
        claims.put("userId", userId);
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new EventException("Se requiere token de autorización");
        }

        return authHeader.substring(7);
    }

    public String getTokenFromRequestForAuth(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Se requiere token de autorización");
        }

        return authHeader.substring(7);
    }

    public Long getUserIdFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);

        Long userId = extractUserId(token);

        if (userId == null) {
            String email = extractUsername(token);
            if (email == null) {
                throw new EventException("Token inválido");
            }
            return userService.getUserByEmail(email).getId();
        }

        return userId;
    }

    public boolean isAdminFromRequest(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            Boolean isAdmin = extractIsAdmin(token);

            return isAdmin != null && isAdmin;
        } catch (EventException e) {
            return false;
        }
    }

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

    public boolean checkUserEventAccessPermission(HttpServletRequest request, Long targetUserId) {
        Long requestingUserId = getUserIdFromRequest(request);
        boolean isAdmin = isAdminFromRequest(request);

        if (!isAdmin && !requestingUserId.equals(targetUserId)) {
            throw new EventException("No tienes permiso para acceder a los eventos de otro usuario");
        }

        return true;
    }


    public Event createEventForUser(HttpServletRequest request, com.blinky.apillama3blinky.controller.dto.EventCreateDTO eventCreateDTO) {
        Long userId = getUserIdFromRequest(request);
        return eventService.createEvent(eventCreateDTO, userId);
    }
}
