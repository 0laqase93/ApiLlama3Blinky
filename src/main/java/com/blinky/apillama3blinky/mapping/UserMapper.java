package com.blinky.apillama3blinky.mapping;

import com.blinky.apillama3blinky.controller.dto.UserDTO;
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
            null,  // Don't include password in response
            user.isAdmin()
        );
    }

    public static List<UserDTO> toDTOList(List<User> users) {
        return users.stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    public static User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setAdmin(userDTO.isAdmin());
        return user;
    }

    public static User updateEntity(User user, UserDTO userDTO) {
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPassword() != null) {
            user.setPassword(userDTO.getPassword());
        }
        user.setAdmin(userDTO.isAdmin());
        return user;
    }
}
