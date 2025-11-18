package com.general.citas.security.JWT;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.general.citas.model.User;
import com.general.citas.model.User.Role;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.access-expiration-minutes}")
    private int accessTokenExpirationMinutes;

    @Value("${jwt.refresh-expiration-days}")
    private int refreshTokenExpirationDays;

    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();

    public String generateAccessToken (User user){

        Instant now = Instant.now();
        Instant exp = now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
            .setSubject(user.getName())
            .claim("uuid", user.getUuid())
            .claim("role", user.getRole().name())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(exp))
            .signWith(getSignatureKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String generateRefreshToken(User user){

        Instant now = Instant.now();
        Instant exp = now.plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

        String refreshToken = Jwts.builder()
            .setSubject(user.getName())
            .claim("uuid" , user.getUuid())
            .claim("role", user.getRole().name())
            .setIssuedAt( Date.from(now))
            .setExpiration(Date.from(exp))
            .signWith(getSignatureKey(), SignatureAlgorithm.HS256)
            .compact();

          refreshTokenStore.put(refreshToken, user.getUuid());
          
          return refreshToken;
    }


    public boolean isTokenValid(String token , UserDetails userDetails) {
        try {
            Claims claims = extractAllClaims(token);

            String username = claims.getSubject();
            if (!username.equals(userDetails.getUsername())){
                return false;
            }

            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                return false;
            }

            return true;

        }catch (JwtException e) {
            logger.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public Role getRoleFromToken(String token) {
    String roleStr = getClaim(token, claims -> claims.get("role", String.class));
    return roleStr != null ? Role.valueOf(roleStr) : null;
    }

    public String getUserUuidFromToken(String token){
        return getClaim(token, claims -> claims.get("uuid" , String.class));
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsFunction) {
        Claims claims = extractAllClaims(token);
        return claimsFunction.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignatureKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignatureKey() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String validateRefreshTokenAndGetUuid(String refreshToken) {
        Claims claims = extractAllClaims(refreshToken);
        String uuid = claims.get("uuid", String.class);
        if (!refreshTokenStore.containsKey(refreshToken) || !refreshTokenStore.get(refreshToken).equals(uuid)) {
            throw new JwtException("Refresh token inválido o revocado");
        }
        return uuid;
    }

    public void revokeRefreshToken(String refreshToken){

        refreshTokenStore.remove(refreshToken);
    }

    public long getRefreshTokenExpirationSeconds () {

        return TimeUnit.DAYS.toSeconds(refreshTokenExpirationDays);
    }
}