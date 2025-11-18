package com.general.citas.security.JWT;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import com.general.citas.security.UsersDetailSeviceImp;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class JwtAuthenticationFilter extends OncePerRequestFilter{

   @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UsersDetailSeviceImp userDetailsService;

    private static final String BEARER = "Bearer ";

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request, 
        @NonNull HttpServletResponse response, 
        @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            
            final String token = authHeader.substring(BEARER.length());
           
            try{
                String username = jwtUtils.getUsernameFromToken(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null ){
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if(jwtUtils.isTokenValid(token, userDetails)){

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                    }
                    
                } catch (JwtException e) {
                logger.warn( e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

        }

        filterChain.doFilter(request, response);
    }
}
