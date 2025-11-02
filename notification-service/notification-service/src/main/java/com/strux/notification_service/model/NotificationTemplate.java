package com.strux.notification_service.model;

import com.strux.notification_service.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

// 2. NotificationTemplate Entity
@Entity
@Table(name = "notification_templates")
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {
    @Id
    private String id;

    private String eventType;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String language = "az";

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    private Boolean isActive = true;
    private LocalDateTime createdAt;

    @ElementCollection
    private Map<String, String> variables;
}

