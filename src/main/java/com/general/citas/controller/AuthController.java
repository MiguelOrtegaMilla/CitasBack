package com.general.citas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.general.citas.DTO.UserRequestDTO;
import com.general.citas.DTO.AuthRequestDTO;
import com.general.citas.DTO.AuthResponseDTO;
import com.general.citas.DTO.UsersDetailsResponseDTO;
import com.general.citas.service.AuthService;
import com.general.citas.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    // Login - Devuelve el token en la cabecera
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO request) {
        AuthResponseDTO response = authService.login(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + response.getToken())
                .body(response);
    }
 
    @PostMapping("/signup")
    public ResponseEntity<UsersDetailsResponseDTO> register(@RequestBody UserRequestDTO request) {
        UsersDetailsResponseDTO response = userService.createUser(request);
        return ResponseEntity.ok(response);
    }
}
