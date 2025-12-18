package com.strux.auth_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strux.auth_service.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorAuthService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;
    private final KeycloakAdminService keycloakAdminService;
    private final ObjectMapper objectMapper;  // ✅ Inject ObjectMapper

    private static final String TWO_FA_PREFIX = "2fa:";
    private static final int CODE_EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 3;
    private final SecureRandom random = new SecureRandom();

    /**
     * Generate 2FA token and send code via email
     */
    public String generateToken(String userId) {
        try {
            String token = UUID.randomUUID().toString();
            String code = String.format("%06d", random.nextInt(1000000));

            // ✅ Use Serializable class
            TwoFAData twoFAData = new TwoFAData(userId, code, 0);

            String key = TWO_FA_PREFIX + token;
            redisTemplate.opsForValue().set(key, twoFAData, CODE_EXPIRY_MINUTES, TimeUnit.MINUTES);

            log.info("2FA code generated for user {}: {}", userId, code);

            // ✅ Send code via email
            try {
                var user = keycloakAdminService.getUserById(userId);
                String email = user.getEmail();

                if (email != null && !email.isEmpty()) {
                    emailService.send2FACode(email, code);
                    log.info("2FA code sent to email: {}", maskEmail(email));
                } else {
                    log.warn("User {} has no email, cannot send 2FA code", userId);
                }
            } catch (Exception e) {
                log.error("Failed to send 2FA email: {}", e.getMessage());
                // Don't fail - in dev, code is still in logs
            }

            return token;

        } catch (Exception e) {
            log.error("2FA token generation error: {}", e.getMessage(), e);
            throw new AuthenticationException("Failed to generate 2FA token", e);
        }
    }

    /**
     * Verify 2FA code with attempt limiting
     */
    public boolean verifyToken(String token, String code) {
        try {
            if (token == null || code == null) {
                log.warn("2FA verification failed: token or code is null");
                return false;
            }

            String key = TWO_FA_PREFIX + token;

            // ✅ Safe deserialization
            TwoFAData twoFAData = getTwoFAData(key);

            if (twoFAData == null) {
                log.warn("2FA token not found or expired: {}", token);
                return false;
            }

            // Check attempts
            if (twoFAData.getAttempts() >= MAX_ATTEMPTS) {
                log.warn("2FA max attempts exceeded for user: {}", twoFAData.getUserId());
                redisTemplate.delete(key);
                return false;
            }

            // Verify code
            String storedCode = twoFAData.getCode();
            if (storedCode != null && storedCode.equals(code)) {
                log.info("2FA verification successful for user: {}", twoFAData.getUserId());
                redisTemplate.delete(key);
                return true;
            }

            // Increment attempts
            twoFAData.setAttempts(twoFAData.getAttempts() + 1);
            redisTemplate.opsForValue().set(key, twoFAData, CODE_EXPIRY_MINUTES, TimeUnit.MINUTES);

            log.warn("Invalid 2FA code for user: {} (attempt {}/{})",
                    twoFAData.getUserId(), twoFAData.getAttempts(), MAX_ATTEMPTS);

            return false;

        } catch (Exception e) {
            log.error("2FA verification error: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get user ID from 2FA token
     */
    public String getUserIdFromToken(String token) {
        try {
            if (token == null) {
                throw new AuthenticationException("2FA token is null");
            }

            String key = TWO_FA_PREFIX + token;

            // ✅ Safe deserialization
            TwoFAData twoFAData = getTwoFAData(key);

            if (twoFAData == null) {
                throw new AuthenticationException("Invalid or expired 2FA token");
            }

            return twoFAData.getUserId();

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting userId from 2FA token: {}", e.getMessage(), e);
            throw new AuthenticationException("Failed to get user from 2FA token", e);
        }
    }

    /**
     * ✅ SAFE DESERIALIZATION METHOD
     * Handles both TwoFAData objects and LinkedHashMap from Redis
     */
    private TwoFAData getTwoFAData(String key) {
        try {
            Object rawData = redisTemplate.opsForValue().get(key);

            if (rawData == null) {
                return null;
            }

            // ✅ If it's already TwoFAData, return it
            if (rawData instanceof TwoFAData) {
                return (TwoFAData) rawData;
            }

            // ✅ If it's a Map (LinkedHashMap), convert it
            if (rawData instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) rawData;

                String userId = (String) map.get("userId");
                String code = (String) map.get("code");
                Integer attempts = (Integer) map.getOrDefault("attempts", 0);

                return new TwoFAData(userId, code, attempts);
            }

            // ✅ Try ObjectMapper as fallback
            return objectMapper.convertValue(rawData, TwoFAData.class);

        } catch (Exception e) {
            log.error("Failed to deserialize 2FA data from key {}: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Resend 2FA code
     */
    public void resendCode(String token) {
        try {
            String key = TWO_FA_PREFIX + token;
            TwoFAData twoFAData = getTwoFAData(key);

            if (twoFAData == null) {
                throw new AuthenticationException("Invalid or expired 2FA token");
            }

            // Generate new code
            String newCode = String.format("%06d", random.nextInt(1000000));
            twoFAData.setCode(newCode);
            twoFAData.setAttempts(0); // Reset attempts

            // Save updated data
            redisTemplate.opsForValue().set(key, twoFAData, CODE_EXPIRY_MINUTES, TimeUnit.MINUTES);

            log.info("2FA code regenerated for user {}: {}", twoFAData.getUserId(), newCode);

            // Send new code via email
            try {
                var user = keycloakAdminService.getUserById(twoFAData.getUserId());
                String email = user.getEmail();

                if (email != null && !email.isEmpty()) {
                    emailService.send2FACode(email, newCode);
                    log.info("New 2FA code sent to email: {}", maskEmail(email));
                }
            } catch (Exception e) {
                log.error("Failed to send 2FA email: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error resending 2FA code: {}", e.getMessage(), e);
            throw new AuthenticationException("Failed to resend 2FA code", e);
        }
    }

    /**
     * Mask email for logging
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 2) {
            return "**@" + domain;
        }

        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
    }

    /**
     * Serializable data class for Redis storage
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class TwoFAData implements Serializable {
        private static final long serialVersionUID = 1L;

        private String userId;
        private String code;
        private int attempts;
    }
}