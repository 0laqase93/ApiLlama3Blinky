package com.blinky.apillama3blinky.service;

import com.blinky.apillama3blinky.controller.response.AuthResponse;
import com.blinky.apillama3blinky.exception.EmailAlreadyInUseException;
import com.blinky.apillama3blinky.exception.InvalidPasswordException;
import com.blinky.apillama3blinky.exception.UserNotFoundException;
import com.blinky.apillama3blinky.mapping.UserMapper;
import com.blinky.apillama3blinky.model.User;
import com.blinky.apillama3blinky.repository.UserRepository;
import com.blinky.apillama3blinky.security.JwtUtil;
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

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public User registerUser(User user) {
        // Check if user with the same email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException("Email already in use");
        }
        // Set isAdmin to false by default for new registrations
        user.setAdmin(false);
        return userRepository.save(user);
    }

    @Transactional
    public AuthResponse registerUserFromDTO(com.blinky.apillama3blinky.controller.dto.RegisterDTO registerDTO) {
        // Check if user with the same email already exists
        if (userRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException("Email already in use");
        }

        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setPassword(registerDTO.getPassword());
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

        // Generate JWT token
        String token = jwtUtil.generateToken(userDetails);

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

            // Generate JWT token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            // Get the user from the database
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

            // Return the token and user information
            return new AuthResponse(token, UserMapper.toDTO(user));
        } catch (Exception e) {
            // Handle authentication failure
            if (e.getMessage().contains("Bad credentials")) {
                throw new InvalidPasswordException("Invalid password");
            }
            throw e;
        }
    }
}
