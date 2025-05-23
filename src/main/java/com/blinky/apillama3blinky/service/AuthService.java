package com.blinky.apillama3blinky.service;

import com.blinky.apillama3blinky.controller.dto.ResetPasswordDTO;
import com.blinky.apillama3blinky.controller.response.AuthResponse;
import com.blinky.apillama3blinky.exception.EmailAlreadyInUseException;
import com.blinky.apillama3blinky.exception.InvalidPasswordException;
import com.blinky.apillama3blinky.exception.ResourceNotFoundException;
import com.blinky.apillama3blinky.mapping.UserMapper;
import com.blinky.apillama3blinky.model.User;
import com.blinky.apillama3blinky.repository.UserRepository;
import com.blinky.apillama3blinky.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, AuthenticationManager authenticationManager, UserService userService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    @Transactional
    public User registerUser(User user) {
        // Check if user with the same email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException("El correo electrónico ya está en uso");
        }
        // Set isAdmin to false by default for new registrations
        user.setAdmin(false);
        return userRepository.save(user);
    }

    @Transactional
    public AuthResponse registerUserFromDTO(com.blinky.apillama3blinky.controller.dto.RegisterDTO registerDTO) {
        // Check if user with the same email already exists
        if (userRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException("El correo electrónico ya está en uso");
        }

        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setPassword(registerDTO.getPassword());
        user.setUsername(registerDTO.getUsername());
        // Set isAdmin to false by default for new registrations
        user.setAdmin(false);

        User savedUser = userRepository.save(user);

        // Create UserDetails for the new user
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        savedUser.getEmail(),
                        savedUser.getPassword(),
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
                );

        // Generate JWT token with admin status and userId
        String token = jwtUtil.generateToken(userDetails, savedUser.isAdmin(), savedUser.getId());

        // Return the token and user information
        return new AuthResponse(token, UserMapper.toDTO(savedUser));
    }

    @Transactional(readOnly = true)
    public AuthResponse loginUser(String email, String password) {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // Get the user from the database
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

            // Generate JWT token with admin status and userId
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails, user.isAdmin(), user.getId());

            // Return the token and user information
            return new AuthResponse(token, UserMapper.toDTO(user));
        } catch (Exception e) {
            // Handle authentication failure
            if (e.getMessage().contains("Bad credentials")) {
                throw new InvalidPasswordException("Contraseña inválida");
            }
            throw e;
        }
    }

    @Transactional
    public User resetPasswordFromToken(String token, String newPassword) {
        // Extract the email from the token
        String email = jwtUtil.extractUsername(token);
        if (email == null) {
            throw new IllegalArgumentException("Token inválido");
        }

        // Reset the password
        return userService.resetPassword(email, newPassword);
    }

    @Transactional
    public User resetPasswordFromRequest(HttpServletRequest request, String newPassword) {
        // Extract the token from the Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Se requiere token de autorización");
        }

        // Extract the token
        String token = authHeader.substring(7);

        // Reset the password using the token
        return resetPasswordFromToken(token, newPassword);
    }
    @Transactional(readOnly = true)
    public boolean verifyPasswordFromToken(String token, String password) {
        // Extract the email from the token
        String email = jwtUtil.extractUsername(token);
        if (email == null) {
            throw new IllegalArgumentException("Token inválido");
        }

        // Get the user from the database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        // Compare the provided password with the stored password
        return user.getPassword().equals(password);
    }

    @Transactional(readOnly = true)
    public boolean verifyPasswordFromRequest(HttpServletRequest request, String password) {
        // Extract the token from the Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Se requiere token de autorización");
        }

        // Extract the token
        String token = authHeader.substring(7);

        // Verify the password using the token
        return verifyPasswordFromToken(token, password);
    }
}
