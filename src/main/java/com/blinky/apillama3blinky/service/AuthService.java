package com.blinky.apillama3blinky.service;

import com.blinky.apillama3blinky.controller.dto.LoginDTO;
import com.blinky.apillama3blinky.controller.dto.ResetPasswordDTO;
import com.blinky.apillama3blinky.controller.dto.VerifyPasswordDTO;
import com.blinky.apillama3blinky.controller.response.AuthResponse;
import com.blinky.apillama3blinky.exception.EmailAlreadyInUseException;
import com.blinky.apillama3blinky.exception.InvalidPasswordException;
import com.blinky.apillama3blinky.exception.ResourceNotFoundException;
import com.blinky.apillama3blinky.mapping.UserMapper;
import com.blinky.apillama3blinky.model.User;
import com.blinky.apillama3blinky.repository.UserRepository;
import com.blinky.apillama3blinky.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for user authentication and registration.
 * Provides methods for user registration, login, and password management.
 */
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

    /**
     * Registers a new user in the system.
     * 
     * @param user The user entity to register
     * @return The registered user entity with generated ID
     * @throws EmailAlreadyInUseException if the email is already registered
     */
    @Transactional
    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException("El correo electrónico ya está en uso");
        }
        user.setAdmin(false);
        return userRepository.save(user);
    }

    /**
     * Registers a new user from a DTO and generates a JWT token.
     * 
     * @param registerDTO Data transfer object containing registration information
     * @return Authentication response containing the JWT token and user details
     * @throws EmailAlreadyInUseException if the email is already registered
     */
    @Transactional
    public AuthResponse registerUserFromDTO(com.blinky.apillama3blinky.controller.dto.RegisterDTO registerDTO) {
        if (userRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException("El correo electrónico ya está en uso");
        }

        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setPassword(registerDTO.getPassword());
        user.setUsername(registerDTO.getUsername());
        user.setAdmin(false);

        User savedUser = userRepository.save(user);

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        savedUser.getEmail(),
                        savedUser.getPassword(),
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
                );

        String token = jwtUtil.generateToken(userDetails, savedUser.isAdmin(), savedUser.getId());

        return new AuthResponse(token, UserMapper.toDTO(savedUser));
    }

    /**
     * Authenticates a user and generates a JWT token.
     * 
     * @param loginDTO Data transfer object containing login credentials
     * @return Authentication response containing the JWT token and user details
     * @throws ResourceNotFoundException if the user is not found
     * @throws InvalidPasswordException if the password is incorrect
     */
    @Transactional(readOnly = true)
    public AuthResponse loginUser(LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
            );

            User user = userRepository.findByEmail(loginDTO.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + loginDTO.getEmail()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails, user.isAdmin(), user.getId());

            return new AuthResponse(token, UserMapper.toDTO(user));
        } catch (Exception e) {
            if (e.getMessage().contains("Bad credentials")) {
                throw new InvalidPasswordException("Contraseña inválida");
            }
            throw e;
        }
    }

    /**
     * Resets a user's password and returns an appropriate HTTP response.
     * 
     * @param resetPasswordDTO Data transfer object containing the new password
     * @param request The HTTP request containing the JWT token for user authentication
     * @return Response entity indicating success or failure of the password reset
     */
    @Transactional
    public ResponseEntity<Boolean> resetPasswordWithResponse(ResetPasswordDTO resetPasswordDTO, HttpServletRequest request) {
        return jwtUtil.resetPasswordWithResponse(request, resetPasswordDTO.getNewPassword());
    }

    /**
     * Verifies if a provided password matches the user's current password.
     * 
     * @param verifyPasswordDTO Data transfer object containing the password to verify
     * @param request The HTTP request containing the JWT token for user authentication
     * @return Response entity indicating if the password is valid
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Boolean> verifyPasswordWithResponse(VerifyPasswordDTO verifyPasswordDTO, HttpServletRequest request) {
        return jwtUtil.verifyPasswordWithResponse(request, verifyPasswordDTO.getPassword());
    }

    /**
     * Simple health check endpoint to verify the service is running.
     * 
     * @return Response entity with a "Pong!" message
     */
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Pong!");
    }
}
