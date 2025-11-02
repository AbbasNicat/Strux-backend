package com.strux.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceFingerprintService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String DEVICE_PREFIX = "device:";
    private static final int TRUST_DAYS = 30;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public void registerDevice(String userId, String deviceFingerprint, String ipAddress, String userAgent) {
        if (deviceFingerprint == null || deviceFingerprint.isEmpty()) {
            return;
        }

        String key = DEVICE_PREFIX + userId + ":" + deviceFingerprint;

        Map<String, Object> deviceInfo = new HashMap<>();
        deviceInfo.put("ipAddress", ipAddress);
        deviceInfo.put("userAgent", userAgent);
        deviceInfo.put("firstSeen", LocalDateTime.now().format(FORMATTER)); // String'ə çevir
        deviceInfo.put("trusted", false);

        redisTemplate.opsForValue().set(key, deviceInfo, TRUST_DAYS, TimeUnit.DAYS);
        log.info("Device registered for user: {}", userId);
    }

    public boolean isDeviceTrusted(String userId, String deviceFingerprint) {
        if (deviceFingerprint == null || deviceFingerprint.isEmpty()) {
            return false;
        }

        String key = DEVICE_PREFIX + userId + ":" + deviceFingerprint;
        Map<String, Object> deviceInfo = (Map<String, Object>) redisTemplate.opsForValue().get(key);

        if (deviceInfo == null) {
            return false;
        }

        return Boolean.TRUE.equals(deviceInfo.get("trusted"));
    }

    public void trustDevice(String userId, String deviceFingerprint) {
        if (deviceFingerprint == null || deviceFingerprint.isEmpty()) {
            return;
        }

        String key = DEVICE_PREFIX + userId + ":" + deviceFingerprint;
        Map<String, Object> deviceInfo = (Map<String, Object>) redisTemplate.opsForValue().get(key);

        if (deviceInfo != null) {
            deviceInfo.put("trusted", true);
            deviceInfo.put("trustedAt", LocalDateTime.now().format(FORMATTER)); // String'ə çevir
            redisTemplate.opsForValue().set(key, deviceInfo, TRUST_DAYS, TimeUnit.DAYS);
            log.info("Device trusted for user: {}", userId);
        }
    }
}