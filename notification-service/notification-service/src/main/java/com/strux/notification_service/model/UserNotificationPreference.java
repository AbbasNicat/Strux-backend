package com.strux.notification_service.model;


import com.strux.notification_service.enums.NotificationCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "user_notification_preferences")
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class UserNotificationPreference {
    @Id
    private String id;

    private String userId;

    @Enumerated(EnumType.STRING)
    private NotificationCategory category;

    private Boolean emailEnabled = true;
    private Boolean smsEnabled = false;
    private Boolean pushEnabled = true;
    private Boolean inAppEnabled = true;
    private LocalDateTime  createdAt;

    // Specific event preferences
    @ElementCollection
    private Map<String, Boolean> eventPreferences;
}
