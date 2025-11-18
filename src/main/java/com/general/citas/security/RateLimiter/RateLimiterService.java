package com.general.citas.security.RateLimiter;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * Servicio que mantiene contadores en memoria por clave (userId + endpoint).
 *
 * Implementación basada en "sliding window" simple: para cada clave se mantiene
 * una deque de timestamps (epoch millis) de las peticiones recientes. Al recibir
 * una petición se expiran timestamps antiguos (fuera de la ventana) y se comprueba
 * el tamaño de la deque.
 *
 * Ventajas: sencillo, con memoria proporcional al número de peticiones en la ventana.
 * Limitaciones: no persistente, no distribuido, memoria local.
 */


@Service
public class RateLimiterService {

    private final Map<String, Deque<Long>> requestsMap = new ConcurrentHashMap<>();

    private final RateLimitsProperties properties;

    public RateLimiterService (RateLimitsProperties properties){
        this.properties = properties;
    }

    /**
     * Comprueba si la petición está permitida y si lo está registra la petición.
     */
    public boolean tryConsume(String userUuid , String endpointPath){

        RateLimitsProperties.Limit limit = resolveLimitForEndpoint(endpointPath);

        long windowMillis = limit.getWindowSeconds() * 1000;
        int maxRequests = limit.getRequests();
        long now = Instant.now().toEpochMilli();

        String key = buildKey(userUuid , endpointPath);

        Deque<Long> deque = requestsMap.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (deque) {
         // eliminar timestamps fuera de la ventana
            expireOldRequests(deque, now - windowMillis);

            if (deque.size() < maxRequests) {
                // permitir y registrar timestamp
                deque.addLast(now);
                return true;
            } else {
                // límite alcanzado
                return false;
            }
        }
    }

    private void expireOldRequests(Deque<Long> deque, long cutoffMillis) {
        while (!deque.isEmpty() && deque.peekFirst() < cutoffMillis) {
            deque.pollFirst();
        }
    }

    private RateLimitsProperties.Limit resolveLimitForEndpoint(String endpointPath) {
        if (properties.getOverrides() != null && properties.getOverrides().containsKey(endpointPath)) {
            return properties.getOverrides().get(endpointPath);
        }
        return properties.getDefaultLimit();
    }

    private String buildKey(String userUuid, String endpointPath) {
        return userUuid + "::" + endpointPath;
    }
}
