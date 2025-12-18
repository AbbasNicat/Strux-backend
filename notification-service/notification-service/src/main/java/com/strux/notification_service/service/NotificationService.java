package com.strux.notification_service.service;

import com.strux.notification_service.dto.NotificationEventDTO;
import com.strux.notification_service.enums.NotificationCategory;
import com.strux.notification_service.enums.NotificationStatus;
import com.strux.notification_service.enums.NotificationType;
import com.strux.notification_service.model.Notification;
import com.strux.notification_service.model.NotificationTemplate;
import com.strux.notification_service.model.UserNotificationPreference;
import com.strux.notification_service.repository.NotificationRepository;
import com.strux.notification_service.repository.NotificationTemplateRepository;
import com.strux.notification_service.repository.UserNotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final UserNotificationPreferenceRepository preferenceRepository;
    private final TemplateService templateService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ✅ YENİ: Duplicate detection cache
    private final Map<String, LocalDateTime> recentNotifications = new ConcurrentHashMap<>();
    private static final long DUPLICATE_THRESHOLD_SECONDS = 10;

    public void processEvent(NotificationEventDTO event) {
        log.info("Processing notification event: {}", event.getEventType());

        final List<String> recipients;
        if (event.getUserIds() != null && !event.getUserIds().isEmpty()) {
            recipients = event.getUserIds();
        } else if (event.getUserId() != null && !event.getUserId().isEmpty()) {
            recipients = List.of(event.getUserId());
        } else {
            log.warn("No recipients found for event: {}", event.getEventType());
            return;
        }

        NotificationCategory category = NotificationCategory.ALL;
        try {
            if (event.getCategory() != null) {
                if (event.getCategory() instanceof NotificationCategory) {
                    category = (NotificationCategory) event.getCategory();
                } else {
                    category = NotificationCategory.valueOf(event.getCategory().toString());
                }
            }
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid category: {}, using ALL", event.getCategory());
            category = NotificationCategory.ALL;
        }

        for (String userId : recipients) {
            // ✅ Duplicate kontrolü
            String notificationKey = generateNotificationKey(userId, event);

            if (isDuplicate(notificationKey)) {
                log.warn("🔴 DUPLICATE NOTIFICATION DETECTED - Skipping: user={}, event={}, key={}",
                        userId, event.getEventType(), notificationKey);
                continue;
            }

            // Cache'e ekle
            recentNotifications.put(notificationKey, LocalDateTime.now());

            UserNotificationPreference pref = getOrCreatePreference(userId);

            for (NotificationType channel : event.getChannels()) {
                if (isChannelEnabled(pref, channel.name(), category)) {
                    sendNotification(userId, event, channel);
                } else {
                    log.debug("Channel {} disabled by preference for user {}", channel, userId);
                }
            }
        }

        // ✅ Periodic cleanup
        cleanupOldNotifications();
    }

    // ✅ YENİ: Generate unique key for notification
    private String generateNotificationKey(String userId, NotificationEventDTO event) {
        Map<String, Object> data = event.getData();
        String extraKey = "";

        if (data != null) {
            // Entity ID'yi bul (öncelik sırasına göre)
            if (data.containsKey("unitId")) {
                extraKey = ":" + data.get("unitId").toString();
            } else if (data.containsKey("taskId")) {
                extraKey = ":" + data.get("taskId").toString();
            } else if (data.containsKey("issueId")) {
                extraKey = ":" + data.get("issueId").toString();
            } else if (data.containsKey("workerId")) {
                extraKey = ":" + data.get("workerId").toString();
            } else if (data.containsKey("projectId")) {
                extraKey = ":" + data.get("projectId").toString();
            }
        }

        return String.format("%s:%s%s", userId, event.getEventType(), extraKey);
    }

    // ✅ YENİ: Check if notification is duplicate
    private boolean isDuplicate(String notificationKey) {
        LocalDateTime lastSent = recentNotifications.get(notificationKey);

        if (lastSent == null) {
            return false;
        }

        long secondsSinceLastSent = java.time.Duration.between(lastSent, LocalDateTime.now()).getSeconds();
        boolean isDupe = secondsSinceLastSent < DUPLICATE_THRESHOLD_SECONDS;

        if (isDupe) {
            log.debug("Duplicate detected: {} (last sent {} seconds ago)",
                    notificationKey, secondsSinceLastSent);
        }

        return isDupe;
    }

    // ✅ YENİ: Cleanup old entries from cache
    private void cleanupOldNotifications() {
        if (recentNotifications.size() < 1000) {
            return; // Cache henüz küçük, cleanup'a gerek yok
        }

        LocalDateTime threshold = LocalDateTime.now().minusSeconds(DUPLICATE_THRESHOLD_SECONDS * 2);
        int sizeBefore = recentNotifications.size();

        recentNotifications.entrySet().removeIf(entry ->
                entry.getValue().isBefore(threshold)
        );

        int sizeAfter = recentNotifications.size();
        log.debug("🧹 Cleanup: removed {} old entries, {} remaining",
                sizeBefore - sizeAfter, sizeAfter);
    }

    private void sendNotification(String userId, NotificationEventDTO event, NotificationType channel) {
        try {
            NotificationTemplate template = findTemplate(event.getEventType(), channel);

            Map<String, Object> data = event.getData() != null ? event.getData() : Collections.emptyMap();
            String title   = templateService.render(template.getSubject(), data);
            String message = templateService.render(template.getBody(), data);

            Notification n = Notification.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .companyId(event.getCompanyId())
                    .type(channel)
                    .category(resolveCategory(event))
                    .eventType(event.getEventType())
                    .title(title)
                    .message(message)
                    .data(data)
                    .status(NotificationStatus.PENDING)
                    .templateId(template.getId())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Kaydet (PENDING)
            n = notificationRepository.save(n);

            boolean sentOk = sendThroughChannel(n, channel);

            if (sentOk) {
                n.setStatus(NotificationStatus.SENT);
                n.setSentAt(LocalDateTime.now());
                notificationRepository.save(n);
                publishNotificationSent(n);
            } else {
                n.setStatus(NotificationStatus.FAILED);
                n.setFailureReason("Channel returned false");
                notificationRepository.save(n);
                publishNotificationFailed(n);
            }

        } catch (Exception e) {
            log.error("Error in sendNotification(user={}, channel={}): {}", userId, channel, e.toString(), e);
            // HATA FIRLATMADAN FAILURE olarak kaydet
            try {
                Map<String, Object> data = event.getData() != null ? event.getData() : Collections.emptyMap();
                Notification fail = Notification.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .companyId(event.getCompanyId())
                        .type(channel)
                        .category(resolveCategory(event))
                        .eventType(event.getEventType())
                        .title("Failed")
                        .message("Failed to send notification")
                        .data(data)
                        .status(NotificationStatus.FAILED)
                        .failureReason(trimReason(e.getMessage()))
                        .isRead(false)
                        .retryCount(0)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(fail);
                publishNotificationFailed(fail);
            } catch (Exception swallow) {
                log.warn("Failure handler also failed: {}", swallow.toString());
            }
        }
    }

    private boolean sendThroughChannel(Notification n, NotificationType channel) {
        log.info("Sending {} notification to user: {}", channel, n.getUserId());

        if (channel == NotificationType.IN_APP) {
            // IN-APP için dış servis yok, kaydın DB'de kalması yeterli
            return true;
        }

        try {
            return true;
        } catch (Exception ex) {
            log.error("Channel {} send error: {}", channel, ex.toString(), ex);
            return false;
        }
    }

    private NotificationTemplate findTemplate(String eventType, NotificationType channel) {
        return templateRepository.findByEventTypeAndType(eventType, channel)
                .orElseGet(() -> createDefaultTemplate(eventType, channel));
    }

    private NotificationTemplate createDefaultTemplate(String eventType, NotificationType channel) {
        String subject = getDefaultSubject(eventType);
        String body    = getDefaultBody(eventType);

        NotificationTemplate t = NotificationTemplate.builder()
                .id(UUID.randomUUID().toString())
                .eventType(eventType)
                .type(channel)
                .language("az")
                .subject(subject)
                .body(body)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        return templateRepository.save(t);
    }

    private String getDefaultSubject(String eventType) {
        switch (eventType) {
            // Issue events
            case "issue.created":            return "Yeni Issue";
            case "issue.assigned":           return "Sizə yeni məsələ təyin edildi";
            case "issue.updated":            return "Issue yeniləndi";
            case "issue.resolved":           return "Issue həll edildi";
            case "issue.closed":             return "Issue bağlandı";

            // Task events - Worker notifications
            case "task.created":             return "Yeni task yaradıldı";
            case "task.assigned":            return "Yeni task təyinatı";
            case "task.completed.worker.confirmation": return "Task tamamlama təsdiqi";
            case "task.approved":            return "✅ Task təsdiq edildi";
            case "task.rejected":            return "❌ Task rədd edildi";
            case "task.status.changed":      return "Task statusu dəyişdi";
            case "task.progress.milestone":  return "Task irəliləyiş bildirişi";

            // Task events - Admin notifications
            case "task.completed.pending.approval": return "⏳ Task təsdiq gözləyir";
            case "task.progress.updated":    return "📊 Task irəliləməsi yeniləndi";
            case "task.assigned.confirmation": return "Task təyin edildi";

            // User events
            case "user.registered":          return "Xoş gəldiniz";

            // Project events
            case "project.budget.exceeded":  return "Büdcə aşıldı";

            // Worker events
            case "worker.assigned.to.unit":  return "🏗️ Unit-ə təyin edildiniz";
            case "worker.removed.from.unit": return "🚫 Unit-dən çıxarıldınız";
            case "worker.assigned.to.unit.confirmation": return "✅ İşçi unit-ə təyin edildi";
            case "worker.removed.from.unit.confirmation": return "✅ İşçi unit-dən çıxarıldı";

            default:                         return "Bildiriş";
        }
    }

    private String getDefaultBody(String eventType) {
        switch (eventType) {
            // Issue events
            case "issue.created":            return "Yeni issue yaradıldı: {{title}}";
            case "issue.assigned":           return "Issue #{{issueId}} sizə təyin edildi";
            case "issue.updated":            return "Issue #{{issueId}} yeniləndi";
            case "issue.resolved":           return "Issue #{{issueId}} həll edildi";
            case "issue.closed":             return "Issue #{{issueId}} bağlandı";

            // Task events - Worker notifications
            case "task.created":             return "Task #{{taskId}} yaradıldı: {{title}}";
            case "task.assigned":            return "Task #{{taskId}} sizə təyin edildi";
            case "task.completed.worker.confirmation": return "Task #{{taskId}} tamamlama sorğunuz göndərildi və təsdiq gözləyir";
            case "task.approved":            return "✅ Task #{{taskId}} tamamlanması təsdiq edildi. Təşəkkür edirik!";
            case "task.rejected":            return "❌ Task #{{taskId}} rədd edildi. Səbəb: {{rejectionReason}}";
            case "task.status.changed":      return "Task #{{taskId}} statusu {{newStatus}} oldu";
            case "task.progress.milestone":  return "Task #{{taskId}} %{{progressPercentage}} tamamlandı";

            // Task events - Admin notifications
            case "task.completed.pending.approval": return "⏳ İşçi task #{{taskId}}: {{title}} tamamladı və sizin təsdiqqinizi gözləyir";
            case "task.progress.updated":    return "📊 Task #{{taskId}}: {{title}} irəliləməsi %{{progressPercentage}} oldu";
            case "task.assigned.confirmation": return "Task #{{taskId}} uğurla təyin edildi";

            // User events
            case "user.registered":          return "Xoş gəldiniz! Hesabınız yaradıldı.";

            // Project events
            case "project.budget.exceeded":  return "Layihə büdcəsi aşıldı";

            // Worker events
            case "worker.assigned.to.unit":  return "{{workerName}}, siz {{unitName}} unit-inə təyin edildiniz";
            case "worker.removed.from.unit": return "{{workerName}}, siz {{unitName}} unit-indən çıxarıldınız";
            case "worker.assigned.to.unit.confirmation": return "{{workerName}} uğurla {{unitName}} unit-inə təyin edildi";
            case "worker.removed.from.unit.confirmation": return "{{workerName}} {{unitName}} unit-indən çıxarıldı";

            default:                         return "Yeni bildirişiniz var";
        }
    }

    private UserNotificationPreference getOrCreatePreference(String userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    try {
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
                    } catch (DataIntegrityViolationException e) {
                        // Duplicate hatası gelirse tekrar oku
                        return preferenceRepository.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException("Failed to create preference"));
                    }
                });
    }

    private boolean isChannelEnabled(UserNotificationPreference pref, String channel, NotificationCategory category) {
        switch (channel) {
            case "EMAIL": return Boolean.TRUE.equals(pref.getEmailEnabled());
            case "SMS":   return Boolean.TRUE.equals(pref.getSmsEnabled());
            case "PUSH":  return Boolean.TRUE.equals(pref.getPushEnabled());
            case "IN_APP":return Boolean.TRUE.equals(pref.getInAppEnabled());
            default:      return false;
        }
    }

    private void publishNotificationSent(Notification n) {
        try {
            Map<String, Object> evt = new HashMap<>();
            evt.put("notificationId", n.getId());
            evt.put("userId", n.getUserId());
            evt.put("type", n.getType().name());
            evt.put("eventType", n.getEventType());
            evt.put("sentAt", n.getSentAt());
            kafkaTemplate.send("notification.sent", evt);
        } catch (Exception e) {
            log.warn("publishNotificationSent failed: {}", e.toString());
        }
    }

    private void publishNotificationFailed(Notification n) {
        try {
            Map<String, Object> evt = new HashMap<>();
            evt.put("notificationId", n.getId());
            evt.put("userId", n.getUserId());
            evt.put("type", n.getType().name());
            evt.put("eventType", n.getEventType());
            evt.put("reason", n.getFailureReason());
            kafkaTemplate.send("notification.failed", evt);
        } catch (Exception e) {
            // Broker/Topic olmasa bile ana akışı bozma
            log.warn("publishNotificationFailed failed: {}", e.toString());
        }
    }

    // ---------- User-facing methods ----------

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
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            n.setReadAt(LocalDateTime.now());
            n.setStatus(NotificationStatus.READ);
            notificationRepository.save(n);
        });
    }

    public void markAllAsRead(String userId) {
        List<Notification> unread = getUnreadNotifications(userId);
        for (Notification n : unread) {
            n.setIsRead(true);
            n.setReadAt(LocalDateTime.now());
            n.setStatus(NotificationStatus.READ);
        }
        notificationRepository.saveAll(unread);
    }

    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    // --------- helpers ---------

    private NotificationCategory resolveCategory(NotificationEventDTO event) {
        try {
            if (event.getCategory() instanceof NotificationCategory) {
                return (NotificationCategory) event.getCategory();
            }
            if (event.getCategory() != null) {
                return NotificationCategory.valueOf(event.getCategory().toString());
            }
        } catch (Exception ignored) {}
        return NotificationCategory.ALL;
    }

    private String trimReason(String s) {
        if (s == null) return null;
        return s.length() > 512 ? s.substring(0, 512) : s;
    }
}