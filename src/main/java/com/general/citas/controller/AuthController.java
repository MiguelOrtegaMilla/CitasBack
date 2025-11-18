package com.general.citas.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.general.citas.DTO.UserRequestDTO;
import com.general.citas.DTO.AuthRequestDTO;
import com.general.citas.DTO.AuthResponseDTO;
import com.general.citas.DTO.UsersDetailsResponseDTO;
import com.general.citas.model.User;
import com.general.citas.repository.UserRepository;
import com.general.citas.security.JWT.JwtUtils;
import com.general.citas.service.AuthService;
import com.general.citas.service.UserService;

import jakarta.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login (
            @RequestBody AuthRequestDTO request ,
            HttpServletResponse response) {
       
        try {

            AuthResponseDTO authResponse = authService.login(request);

            User u = userRepository.findByName(request.getUsername())
                .orElseThrow();

            String refreshToken = jwtUtils.generateRefreshToken(u);

            //Guardar refresh token en cookie HttpOnly
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken" , refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("api/auth/refresh")
                .maxAge(jwtUtils.getRefreshTokenExpirationSeconds())
                .sameSite("Strict")
                .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authResponse.getAccessToken())
                .body(authResponse);
        }
        catch(BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshAccesToken(
        @CookieValue(value = "refreshToken" , required = false) String refreshToken){
        
        if(refreshToken == null || refreshToken.isEmpty()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED , "vuelve a iniciar sesion");
        }

        try{

             String newAccessToken = authService.refreshAccessToken(refreshToken);

            return ResponseEntity.ok(AuthResponseDTO.builder()
            .accessToken(newAccessToken)
            .build());

        }catch(Exception e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido o expirado");
        }
    } 

    @PostMapping("/signup")
    public ResponseEntity<UsersDetailsResponseDTO> register(@RequestBody UserRequestDTO request) {
        UsersDetailsResponseDTO response = userService.createUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "refreshToken" , required = false) 
                                String refreshToken , HttpServletResponse response) {

        if(refreshToken == null || refreshToken.isEmpty()){
            authService.revokeRefreshToken(refreshToken);
            ResponseCookie deleteCookie = ResponseCookie.from(refreshToken, "")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .maxAge(0)
                .sameSite("Strict")
                .build();
            response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        }

        return ResponseEntity.noContent().build();
    }

}
