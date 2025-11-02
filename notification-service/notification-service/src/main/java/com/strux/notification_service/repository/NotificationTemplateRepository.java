package com.strux.notification_service.repository;

import com.strux.notification_service.enums.NotificationType;
import com.strux.notification_service.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, String> {
    Optional<NotificationTemplate> findByEventTypeAndTypeAndLanguage(
            String eventType, NotificationType type, String language
    );
    Optional<NotificationTemplate> findByEventTypeAndType(String eventType, NotificationType type);
}