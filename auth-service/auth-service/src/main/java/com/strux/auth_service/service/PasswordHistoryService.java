package com.strux.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordHistoryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PASSWORD_HISTORY_PREFIX = "pwd_history:";
    private static final int HISTORY_SIZE = 5;
    private static final int HISTORY_DAYS = 365;

    // Common passwords list (top 10000 most common passwords)
    private static final Set<String> COMMON_PASSWORDS = new HashSet<>(Arrays.asList(
            "123456", "password", "123456789", "12345678", "12345",
            "1234567", "password1", "123123", "1234567890", "000000",
            "abc123", "111111", "qwerty", "welcome", "admin"
            // Add more common passwords here
    ));

    public boolean isCommonPassword(String password) {
        return COMMON_PASSWORDS.contains(password.toLowerCase());
    }

    public void addPasswordToHistory(String userId, String passwordHash) {
        String key = PASSWORD_HISTORY_PREFIX + userId;
        List<String> history = (List<String>) redisTemplate.opsForValue().get(key);

        if (history == null) {
            history = new ArrayList<>();
        }

        history.add(0, passwordHash);

        if (history.size() > HISTORY_SIZE) {
            history = history.subList(0, HISTORY_SIZE);
        }

        redisTemplate.opsForValue().set(key, history, HISTORY_DAYS, TimeUnit.DAYS);
    }

    public boolean isPasswordInHistory(String userId, String password, PasswordEncoder encoder) {
        String key = PASSWORD_HISTORY_PREFIX + userId;
        List<String> history = (List<String>) redisTemplate.opsForValue().get(key);

        if (history == null || history.isEmpty()) {
            return false;
        }

        for (String oldHash : history) {
            if (encoder.matches(password, oldHash)) {
                return true;
            }
        }

        return false;
    }
}
