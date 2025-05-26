package com.blinky.apillama3blinky.controller;

import com.blinky.apillama3blinky.controller.dto.UserDTO;
import com.blinky.apillama3blinky.controller.dto.UserUpdateDTO;
import com.blinky.apillama3blinky.controller.response.UserResponseDTO;
import com.blinky.apillama3blinky.mapping.UserMapper;
import com.blinky.apillama3blinky.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for user management API endpoints.
 * Provides CRUD operations for user entities.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves all users in the system.
     * 
     * @return Response entity with a list of all users
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(UserMapper.toResponseDTOList(userService.getAllUsers()));
    }

    /**
     * Retrieves a specific user by their ID.
     * 
     * @param id The ID of the user to retrieve
     * @return Response entity with the requested user's details
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(UserMapper.toResponseDTO(userService.getUserById(id)));
    }

    /**
     * Retrieves a specific user by their email address.
     * 
     * @param email The email address of the user to retrieve
     * @return Response entity with the requested user's details
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(UserMapper.toResponseDTO(userService.getUserByEmail(email)));
    }

    /**
     * Creates a new user in the system.
     * 
     * @param userDTO Data transfer object containing the new user's details
     * @return Response entity with the created user's details and HTTP 201 Created status
     */
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        return new ResponseEntity<>(UserMapper.toResponseDTO(userService.createUserFromDTO(userDTO)), HttpStatus.CREATED);
    }

    /**
     * Updates an existing user's information.
     * 
     * @param id The ID of the user to update
     * @param userUpdateDTO Data transfer object containing the updated user details
     * @return Response entity with the updated user's details
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        return ResponseEntity.ok(UserMapper.toResponseDTO(userService.updateUser(id, userUpdateDTO)));
    }

    /**
     * Deletes a user from the system.
     * 
     * @param id The ID of the user to delete
     * @return Response entity with HTTP 204 No Content status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
