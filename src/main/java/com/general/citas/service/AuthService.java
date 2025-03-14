package com.general.citas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.general.citas.DTO.AuthRequestDTO;
import com.general.citas.DTO.AuthResponseDTO;
import com.general.citas.model.User;
import com.general.citas.repository.UserRepository;
import com.general.citas.security.JWT.JwtUtils;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    public AuthResponseDTO login(AuthRequestDTO request) {
        try {
                @SuppressWarnings("unused")
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
                );

                User user = userRepository.findByName(request.getUsername())
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                String token = jwtUtils.generateToken(user);

                return new AuthResponseDTO(token);

            } catch (BadCredentialsException e) {
                throw new RuntimeException("Credenciales inválidas.");
            }
    }
}
