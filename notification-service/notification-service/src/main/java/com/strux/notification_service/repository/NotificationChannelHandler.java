package com.strux.notification_service.repository;

import com.strux.notification_service.enums.NotificationType;
import com.strux.notification_service.model.Notification;

public interface NotificationChannelHandler {
    boolean supports(NotificationType type);
    boolean send(Notification notification);
}
