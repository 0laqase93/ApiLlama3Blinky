package com.blinky.apillama3blinky.mapping;

import com.blinky.apillama3blinky.controller.dto.UserDTO;
import com.blinky.apillama3blinky.controller.dto.UserUpdateDTO;
import com.blinky.apillama3blinky.controller.response.UserResponseDTO;
import com.blinky.apillama3blinky.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserDTO(
                user.getId(),
                user.getEmail(),
                null,
                user.isAdmin(),
                user.getUsername()
        );
    }

    public static List<UserDTO> toDTOList(List<User> users) {
        return users.stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    public static User toUser(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setAdmin(userDTO.isAdmin());
        user.setUsername(userDTO.getUsername());
        return user;
    }

    public static User toUser(long id, UserUpdateDTO userUpdateDTO) {
        if (userUpdateDTO == null) {
            return null;
        }

        User user = new User();
        user.setId(id);
        user.setEmail(userUpdateDTO.getEmail());
        user.setPassword(userUpdateDTO.getPassword());
        user.setAdmin(userUpdateDTO.isAdmin());
        user.setUsername(userUpdateDTO.getUsername());
        return user;
    }

    public static UserResponseDTO toResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.isAdmin(),
                user.getUsername()
        );
    }

    public static List<UserResponseDTO> toResponseDTOList(List<User> users) {
        return users.stream()
                .map(UserMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
