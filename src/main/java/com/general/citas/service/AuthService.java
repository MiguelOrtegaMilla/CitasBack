package com.general.citas.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.general.citas.DTO.AuthRequestDTO;
import com.general.citas.DTO.AuthResponseDTO;
import com.general.citas.model.User;
import com.general.citas.repository.UserRepository;
import com.general.citas.security.AccountLocker.AccLockService;
import com.general.citas.security.JWT.JwtUtils;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccLockService accLockService;


    
    /**
     * Login de usuario → genera refresh token en cookie y access token de corta duración.
     */
    public AuthResponseDTO login (AuthRequestDTO request){

          // 1. Recuperar usuario
        User user = userRepository.findByName(request.getUsername())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "credenciales inválidas"));

        String userUuid = user.getUuid();

        // 2. Verificar si usuario está bloqueado antes de autenticar
        if(accLockService.isUserLocked(userUuid)){
            throw new ResponseStatusException(HttpStatus.LOCKED , "cuenta bloqueada");
        }
        
        try{
             // 3. Intento de autenticación
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
             );

            // 4. Login exitoso → reset de intentos fallidos
            accLockService.registerSuccessfulLogin(userUuid);

             // 5. Generar access token (corto plazo)
            String accessToken = jwtUtils.generateAccessToken(user);

            return new AuthResponseDTO(accessToken);

        }catch(BadCredentialsException e){
            // 6. Login fallido → registrar intento fallido
            accLockService.registerFailedAttempt(userUuid);

            // Comprobar si el fallo reciente generó un bloqueo
            if (accLockService.isUserLocked(userUuid)) {
                throw new ResponseStatusException(HttpStatus.LOCKED, "Cuenta bloqueada temporalmente");
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
    }

    /**
     * Refresh token → genera un nuevo access token
     */
     public String refreshAccessToken(String refreshToken) {

        String uuid =jwtUtils.validateRefreshTokenAndGetUuid(refreshToken);

        User u = userRepository.findByUuid(uuid)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario invalido"));

        return jwtUtils.generateAccessToken(u);
    }

    public void revokeRefreshToken(String refreshToken) {
        jwtUtils.revokeRefreshToken(refreshToken);
    }

}
