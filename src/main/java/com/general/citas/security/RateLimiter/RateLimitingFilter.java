package com.general.citas.security.RateLimiter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.general.citas.security.JWT.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Filtro que se ejecuta una vez por petición y aplica la lógica del rate limiter.
 * Usa RateLimiterService para comprobar y registrar la petición.
 *
 * Si se excede el límite devuelve HTTP 429 con un JSON sencillo.
 */

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RateLimitingFilter(RateLimiterService rateLimiterService , JwtUtils jwtUtils) {
        this.rateLimiterService = rateLimiterService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Solo aplicar a usuarios autenticados
        if (auth != null && auth.isAuthenticated()) {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // Extraer la UUID del usuario desde el JWT
                String userUuid = jwtUtils.getUserUuidFromToken(token);

                if (userUuid != null) {
                    String endpointPath = request.getRequestURI();

                    boolean allowed = rateLimiterService.tryConsume(userUuid, endpointPath);

                    if (!allowed) {
                        response.setStatus(429);
                        response.setContentType("application/json");
                        Map<String, Object> body = new HashMap<>();
                        body.put("status", 429);
                        body.put("error", "Too Many Requests");
                        body.put("message", "Rate limit exceeded for this endpoint");
                        response.getWriter().write(objectMapper.writeValueAsString(body));
                        return; // bloqueo inmediato
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

}
