package com.general.citas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.general.citas.DTO.UserRequestDTO;
import com.general.citas.DTO.UsersDetailsResponseDTO;
import com.general.citas.service.UserService;

@RestController
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    private UserService userService;

    //Obtener a un usuario por su uuid
    @GetMapping("/{uuid}")
    public ResponseEntity<UsersDetailsResponseDTO> getUser(@PathVariable String uuid) {
        UsersDetailsResponseDTO response = userService.getUserById(uuid);
        return ResponseEntity.ok(response);
    }

    //Obtener a todos los usuarios
    @GetMapping("/list")
    public ResponseEntity<List<UsersDetailsResponseDTO>> getAllUsers() {
        List<UsersDetailsResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    //Crear un usuario manualmente
    @PostMapping("/create")
    public ResponseEntity<UsersDetailsResponseDTO> createUser(@RequestBody UserRequestDTO request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    //Actualizar los datos de un usuario manualmente
    @PutMapping("/{uuid}")
    public ResponseEntity<UsersDetailsResponseDTO> updateUser(@PathVariable String uuid, @RequestBody UserRequestDTO request) {

        return ResponseEntity.ok(userService.updateUser(uuid, request));
    }

    //eliminar a un usuario por su uuid
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteUser(@PathVariable String uuid) {
        userService.deleteUser(uuid);
        return ResponseEntity.noContent().build();
    }

}
