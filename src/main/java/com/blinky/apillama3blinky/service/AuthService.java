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
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException("El correo electrónico ya está en uso");
        }
        user.setAdmin(false);
        return userRepository.save(user);
    }

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

    @Transactional
    public ResponseEntity<Boolean> resetPasswordWithResponse(ResetPasswordDTO resetPasswordDTO, HttpServletRequest request) {
        return jwtUtil.resetPasswordWithResponse(request, resetPasswordDTO.getNewPassword());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Boolean> verifyPasswordWithResponse(VerifyPasswordDTO verifyPasswordDTO, HttpServletRequest request) {
        return jwtUtil.verifyPasswordWithResponse(request, verifyPasswordDTO.getPassword());
    }

    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Pong!");
    }
}
