package com.strux.notification_service.service;

import com.strux.notification_service.dto.NotificationEventDTO;
import com.strux.notification_service.enums.NotificationCategory;
import com.strux.notification_service.enums.NotificationStatus;
import com.strux.notification_service.enums.NotificationType;
import com.strux.notification_service.model.Notification;
import com.strux.notification_service.model.NotificationTemplate;
import com.strux.notification_service.model.UserNotificationPreference;
import com.strux.notification_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final UserNotificationPreferenceRepository preferenceRepository;

    private final TemplateService templateService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void processEvent(NotificationEventDTO event) {
        log.info("Processing notification event: {}", event.getEventType());

        // Get recipients with null safety
        List<String> recipients;
        if (event.getUserIds() != null && !event.getUserIds().isEmpty()) {
            recipients = event.getUserIds();
        } else if (event.getUserId() != null && !event.getUserId().isEmpty()) {
            recipients = List.of(event.getUserId());
        } else {
            log.warn("No recipients found for event: {}", event.getEventType());
            return; // Recipient yoksa işleme devam etme
        }

        // Convert category String to Enum
        NotificationCategory category;
        try {
            category = event.getCategory() != null ?
                    NotificationCategory.valueOf(event.getCategory().toString()) :
                    NotificationCategory.ALL;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid category: {}, using ALL", event.getCategory());
            category = NotificationCategory.ALL;
        }

        for (String userId : recipients) {
            // Get user preferences
            UserNotificationPreference preference = getOrCreatePreference(userId);

            // Send through enabled channels
            for (NotificationType channel : event.getChannels()) {
                String channelName = channel.name();
                if (isChannelEnabled(preference, channelName, category)) {
                    sendNotification(userId, event, channelName);
                }
            }
        }
    }

    private void sendNotification(String userId, NotificationEventDTO event, String channel) {
        try {
            // Find template
            NotificationTemplate template = findTemplate(event.getEventType(), channel);

            // Render content
            String title = templateService.render(template.getSubject(), event.getData());
            String message = templateService.render(template.getBody(), event.getData());

            // Create notification record
            Notification notification = Notification.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .companyId(event.getCompanyId())
                    .type(NotificationType.valueOf(channel))
                    .category(event.getCategory())
                    .eventType(event.getEventType())
                    .title(title)
                    .message(message)
                    .data(event.getData()) // Map<String, Object> olarak direkt atanıyor
                    .status(NotificationStatus.PENDING)
                    .templateId(template.getId())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notification = notificationRepository.save(notification);

            // Send based on channel type
            boolean sent = sendThroughChannel(notification, channel);

            if (sent) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);

                publishNotificationSent(notification);
            } else {
                throw new RuntimeException("Failed to send notification");
            }

        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
            handleFailure(userId, event, channel, e.getMessage());
        }
    }

    private boolean sendThroughChannel(Notification notification, String channel) {

        log.info("Sending {} notification to user: {}", channel, notification.getUserId());
        if ("IN_APP".equals(channel)) {
            return true;
        }

        return false;
    }

    private NotificationTemplate findTemplate(String eventType, String channel) {
        return templateRepository.findByEventTypeAndType(eventType, NotificationType.valueOf(String.valueOf(NotificationType.valueOf(channel))))
                .orElseGet(() -> createDefaultTemplate(eventType, channel));
    }

    private NotificationTemplate createDefaultTemplate(String eventType, String channel) {
        String subject = getDefaultSubject(eventType);
        String body = getDefaultBody(eventType);

        NotificationTemplate template = NotificationTemplate.builder()
                .id(UUID.randomUUID().toString())
                .eventType(eventType)
                .type(NotificationType.valueOf(channel))
                .language("az")
                .subject(subject)
                .body(body)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        return templateRepository.save(template);
    }

    private String getDefaultSubject(String eventType) {
        switch (eventType) {
            case "issue.created":
                return "Yeni Issue";
            case "issue.assigned":
                return "Sizə yeni məsələ təyin edildi";
            case "issue.updated":
                return "Issue yeniləndi";
            case "issue.resolved":
                return "Issue həll edildi";
            case "issue.closed":
                return "Issue bağlandı";
            case "task.completed":
                return "Task tamamlandı";
            case "user.registered":
                return "Xoş gəldiniz";
            case "project.budget.exceeded":
                return "Büdcə aşıldı";
            default:
                return "Bildiriş";
        }
    }

    private String getDefaultBody(String eventType) {
        switch (eventType) {
            case "issue.created":
                return "Yeni issue yaradıldı: {{title}}";
            case "issue.assigned":
                return "Issue #{{issueId}} sizə təyin edildi";
            case "issue.updated":
                return "Issue #{{issueId}} yeniləndi";
            case "issue.resolved":
                return "Issue #{{issueId}} həll edildi";
            case "issue.closed":
                return "Issue #{{issueId}} bağlandı";
            case "task.completed":
                return "Task tamamlandı";
            case "user.registered":
                return "Xoş gəldiniz! Hesabınız yaradıldı.";
            case "project.budget.exceeded":
                return "Layihə büdcəsi aşıldı";
            default:
                return "Yeni bildirişiniz var";
        }
    }

    private UserNotificationPreference getOrCreatePreference(String userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserNotificationPreference pref = UserNotificationPreference.builder()
                            .id(UUID.randomUUID().toString())
                            .userId(userId)
                            .category(NotificationCategory.ALL)
                            .emailEnabled(true)
                            .smsEnabled(false)
                            .pushEnabled(true)
                            .inAppEnabled(true)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return preferenceRepository.save(pref);
                });
    }

    private boolean isChannelEnabled(UserNotificationPreference pref, String channel, NotificationCategory category) {
        switch (channel) {
            case "EMAIL":
                return pref.getEmailEnabled();
            case "SMS":
                return pref.getSmsEnabled();
            case "PUSH":
                return pref.getPushEnabled();
            case "IN_APP":
                return pref.getInAppEnabled();
            default:
                return false;
        }
    }

    private void handleFailure(String userId, NotificationEventDTO event, String channel, String reason) {
        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .companyId(event.getCompanyId())
                .type(NotificationType.valueOf(channel))
                .category(event.getCategory())
                .eventType(event.getEventType())
                .title("Failed")
                .message("Failed to send notification")
                .data(event.getData()) // Map<String, Object>
                .status(NotificationStatus.FAILED)
                .failureReason(reason)
                .isRead(false)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        publishNotificationFailed(notification);
    }

    private void publishNotificationSent(Notification notification) {
        Map<String, Object> event = new HashMap<>();
        event.put("notificationId", notification.getId());
        event.put("userId", notification.getUserId());
        event.put("type", notification.getType().name());
        event.put("eventType", notification.getEventType());
        event.put("sentAt", notification.getSentAt());

        kafkaTemplate.send("notification.sent", event);
    }

    private void publishNotificationFailed(Notification notification) {
        Map<String, Object> event = new HashMap<>();
        event.put("notificationId", notification.getId());
        event.put("userId", notification.getUserId());
        event.put("type", notification.getType().name());
        event.put("eventType", notification.getEventType());
        event.put("reason", notification.getFailureReason());

        kafkaTemplate.send("notification.failed", event);
    }

    // User-facing methods

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public Long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification.setStatus(NotificationStatus.READ);
            notificationRepository.save(notification);
        });
    }

    public void markAllAsRead(String userId) {
        List<Notification> unread = getUnreadNotifications(userId);
        unread.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification.setStatus(NotificationStatus.READ);
        });
        notificationRepository.saveAll(unread);
    }

    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}