package com.strux.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorAuthService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String TWO_FA_PREFIX = "2fa:";
    private static final int CODE_EXPIRY_MINUTES = 10;
    private final SecureRandom random = new SecureRandom();

    public String generateToken(String userId) {
        String token = UUID.randomUUID().toString();
        String code = String.format("%06d", random.nextInt(1000000));

        Map<String, Object> twoFAData = new HashMap<>();
        twoFAData.put("userId", userId);
        twoFAData.put("code", code);
        twoFAData.put("attempts", 0);

        String key = TWO_FA_PREFIX + token;
        redisTemplate.opsForValue().set(key, twoFAData, CODE_EXPIRY_MINUTES, TimeUnit.MINUTES);

        // In production, send this code via SMS or Email
        log.info("2FA code generated for user {}: {}", userId, code);

        return token;
    }

    public boolean verifyToken(String token, String code) {
        String key = TWO_FA_PREFIX + token;
        Map<String, Object> twoFAData = (Map<String, Object>) redisTemplate.opsForValue().get(key);

        if (twoFAData == null) {
            return false;
        }

        int attempts = (int) twoFAData.get("attempts");
        if (attempts >= 3) {
            redisTemplate.delete(key);
            return false;
        }

        String storedCode = (String) twoFAData.get("code");
        if (storedCode.equals(code)) {
            redisTemplate.delete(key);
            return true;
        }

        twoFAData.put("attempts", attempts + 1);
        redisTemplate.opsForValue().set(key, twoFAData, CODE_EXPIRY_MINUTES, TimeUnit.MINUTES);

        return false;
    }

    public String getUserIdFromToken(String token) {
        String key = TWO_FA_PREFIX + token;
        Map<String, Object> twoFAData = (Map<String, Object>) redisTemplate.opsForValue().get(key);

        if (twoFAData == null) {
            throw new RuntimeException("2FA token not found");
        }

        return (String) twoFAData.get("userId");
    }
}
