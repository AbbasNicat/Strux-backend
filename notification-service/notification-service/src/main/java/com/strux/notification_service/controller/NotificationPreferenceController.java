package com.strux.notification_service.controller;

import com.strux.notification_service.enums.NotificationCategory;
import com.strux.notification_service.model.UserNotificationPreference;
import com.strux.notification_service.repository.UserNotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications/preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final UserNotificationPreferenceRepository preferenceRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<UserNotificationPreference> getPreferences(@PathVariable String userId) {
        return preferenceRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    UserNotificationPreference defaultPref = UserNotificationPreference.builder()
                            .id(UUID.randomUUID().toString())
                            .userId(userId)
                            .category(NotificationCategory.ALL)
                            .emailEnabled(true)
                            .smsEnabled(false)
                            .pushEnabled(true)
                            .inAppEnabled(true)
                            .createdAt(LocalDateTime.now())
                            .build();
                    UserNotificationPreference saved = preferenceRepository.save(defaultPref);
                    return ResponseEntity.ok(saved);
                });
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserNotificationPreference> updatePreferences(
            @PathVariable String userId,
            @RequestBody UserNotificationPreference preference
    ) {
        return preferenceRepository.findByUserId(userId)
                .map(existing -> {
                    existing.setEmailEnabled(preference.getEmailEnabled());
                    existing.setSmsEnabled(preference.getSmsEnabled());
                    existing.setPushEnabled(preference.getPushEnabled());
                    existing.setInAppEnabled(preference.getInAppEnabled());
                    existing.setEventPreferences(preference.getEventPreferences());
                    return ResponseEntity.ok(preferenceRepository.save(existing));
                })
                .orElseGet(() -> {
                    preference.setId(UUID.randomUUID().toString());
                    preference.setUserId(userId);
                    preference.setCategory(NotificationCategory.ALL);
                    preference.setCreatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(preferenceRepository.save(preference));
                });
    }
}