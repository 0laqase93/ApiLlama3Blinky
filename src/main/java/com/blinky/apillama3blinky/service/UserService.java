package com.blinky.apillama3blinky.service;

import com.blinky.apillama3blinky.controller.dto.UserDTO;
import com.blinky.apillama3blinky.controller.dto.UserUpdateDTO;
import com.blinky.apillama3blinky.exception.ResourceNotFoundException;
import com.blinky.apillama3blinky.mapping.UserMapper;
import com.blinky.apillama3blinky.model.User;
import com.blinky.apillama3blinky.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsible for user management.
 * Provides methods for creating, retrieving, updating, and deleting users,
 * as well as user lookup by email and password management.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all users in the system.
     * 
     * @return A list of all user entities
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieves a specific user by their ID.
     * 
     * @param id The ID of the user to retrieve
     * @return The user entity
     * @throws ResourceNotFoundException if no user is found with the given ID
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    /**
     * Retrieves a specific user by their email address.
     * 
     * @param email The email address of the user to retrieve
     * @return The user entity
     * @throws ResourceNotFoundException if no user is found with the given email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    /**
     * Creates a new user from a data transfer object.
     * 
     * @param userDTO Data transfer object containing the new user's details
     * @return The created user entity with generated ID
     */
    @Transactional
    public User createUserFromDTO(UserDTO userDTO) {
        User user = UserMapper.toUser(userDTO);
        return userRepository.save(user);
    }

    /**
     * Updates an existing user's information.
     * 
     * @param id The ID of the user to update
     * @param userUpdateDTO Data transfer object containing the updated user details
     * @return The updated user entity
     */
    @Transactional
    public User updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User user = UserMapper.toUser(id, userUpdateDTO);
        return userRepository.save(user);
    }

    /**
     * Deletes a user from the system.
     * 
     * @param id The ID of the user to delete
     * @throws ResourceNotFoundException if no user is found with the given ID
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    /**
     * Resets a user's password.
     * 
     * @param email The email address of the user whose password to reset
     * @param newPassword The new password to set
     * @return The updated user entity
     * @throws ResourceNotFoundException if no user is found with the given email
     */
    @Transactional
    public User resetPassword(String email, String newPassword) {
        User user = getUserByEmail(email);
        user.setPassword(newPassword);
        return userRepository.save(user);
    }
}
