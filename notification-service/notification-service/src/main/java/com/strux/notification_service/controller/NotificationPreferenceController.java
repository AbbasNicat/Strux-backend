package com.strux.notification_service.controller;

import com.strux.notification_service.model.UserNotificationPreference;
import com.strux.notification_service.repository.UserNotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications/preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final UserNotificationPreferenceRepository preferenceRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<UserNotificationPreference> getPreferences(@PathVariable String userId) {
        return preferenceRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
                .orElse(ResponseEntity.notFound().build());
    }
}

