package com.strux.notification_service.model;

import com.strux.notification_service.enums.NotificationCategory;
import com.strux.notification_service.enums.NotificationStatus;
import com.strux.notification_service.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

// 1. Notification Entity
@Entity
@Table(name = "notifications")
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    private String id;

    private String userId;
    private String companyId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private NotificationCategory category;

    private String eventType;

    private String title;
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> data;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private Boolean isRead = false;
    private LocalDateTime readAt;

    private LocalDateTime sentAt;
    private Integer retryCount = 0;
    private String failureReason;

    private String templateId;

    private LocalDateTime createdAt;
}

