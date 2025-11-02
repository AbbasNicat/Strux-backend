package com.strux.notification_service.dto;

import com.strux.notification_service.enums.NotificationCategory;
import com.strux.notification_service.enums.NotificationType;
import lombok.Data;

import java.util.List;

@Data
public class BulkNotificationRequest {
    private List<String> userIds;
    private String title;
    private String message;
    private NotificationCategory category;
    private List<NotificationType> channels;
}
