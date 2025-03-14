package com.general.citas.converter;

import org.springframework.stereotype.Component;

import com.general.citas.DTO.UserRequestDTO;
import com.general.citas.DTO.AuthResponseDTO;
import com.general.citas.DTO.UsersDetailsResponseDTO;
import com.general.citas.model.User;



@Component
public class UserConverter {

    public User userRequestDTOtoUserEntity(UserRequestDTO request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword()) // Será codificada antes de guardar
                .phone(request.getPhone())
                .build();
    }

    public UsersDetailsResponseDTO userEntityToUserDetailResponseDTO(User user) {
        return UsersDetailsResponseDTO.builder()
            .uuid(user.getUuid())
            .name(user.getName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .rol(user.getRole())
            .build();
    }

    public AuthResponseDTO userEntityToUserResponseDTO(String token) {
        return AuthResponseDTO.builder()
               .token(token)
                .build();
    }
    

}

 