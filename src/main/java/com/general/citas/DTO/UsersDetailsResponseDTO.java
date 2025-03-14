package com.general.citas.DTO;

import com.general.citas.model.User.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsersDetailsResponseDTO {

    private String uuid;
    private String name;
    private String email;
    private String phone;
    private Role rol;
}
