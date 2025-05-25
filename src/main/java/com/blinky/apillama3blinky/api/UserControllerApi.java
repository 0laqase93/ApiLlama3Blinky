package com.blinky.apillama3blinky.api;

import com.blinky.apillama3blinky.controller.dto.UserDTO;
import com.blinky.apillama3blinky.controller.dto.UserUpdateDTO;
import com.blinky.apillama3blinky.controller.response.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface UserControllerApi {

    ResponseEntity<List<UserResponseDTO>> getAllUsers();

    ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable Long id);

    ResponseEntity<UserResponseDTO> getUserByEmail(
            @PathVariable String email);

    ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody UserDTO userDTO);

    ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO);

    ResponseEntity<Void> deleteUser(
            @PathVariable Long id);
}