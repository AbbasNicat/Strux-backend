package com.strux.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoLocationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final WebClient webClient;
    private static final String LOCATION_PREFIX = "location:";

    public String getCountryCode(String ipAddress) {
        try {
            // Use a free IP geolocation API (replace with your preferred service)
            String response = webClient.get()
                    .uri("https://ipapi.co/" + ipAddress + "/country_code/")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(java.time.Duration.ofSeconds(3))
                    .block();

            return response != null ? response.trim() : "UNKNOWN";
        } catch (Exception e) {
            log.error("Geolocation lookup failed: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    public String getLocation(String ipAddress) {
        try {
            String response = webClient.get()
                    .uri("https://ipapi.co/" + ipAddress + "/json/")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(java.time.Duration.ofSeconds(3))
                    .block();

            return response;
        } catch (Exception e) {
            log.error("Geolocation lookup failed: {}", e.getMessage());
            return "Unknown";
        }
    }

    public String getLastKnownLocation(String userId) {
        String key = LOCATION_PREFIX + userId;
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void updateLocation(String userId, String countryCode) {
        String key = LOCATION_PREFIX + userId;
        redisTemplate.opsForValue().set(key, countryCode, 30, TimeUnit.DAYS);
    }

    public int calculateDistance(String country1, String country2) {
        // Simplified distance calculation
        // In production, use actual lat/long coordinates
        if (country1.equals(country2)) {
            return 0;
        }

        // Different continents = ~10000km
        // Same continent = ~2000km
        // This is a placeholder - implement proper distance calculation
        return 5000;
    }
}
