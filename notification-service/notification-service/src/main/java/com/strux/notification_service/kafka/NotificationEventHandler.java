package com.strux.notification_service.kafka;

import com.strux.notification_service.dto.NotificationEventDTO;
import com.strux.notification_service.enums.NotificationCategory;
import com.strux.notification_service.enums.NotificationType;
import com.strux.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventHandler {

    private final NotificationService notificationService;

    // ============ ISSUE EVENTS ============

    @KafkaListener(topics = "issue.created", groupId = "notification-service")
    public void handleIssueCreated(@Payload Map<String, Object> event) {
        log.info("Received issue.created event: {}", event);

        String userId = getStringValue(event, "userId");

        // userId null kontrolü
        if (userId == null || userId.isEmpty()) {
            log.warn("No userId in issue.created event, skipping notification");
            return;
        }

        NotificationEventDTO notification = NotificationEventDTO.builder()
                .eventType("issue.created")
                .userId(userId)
                .companyId(getStringValue(event, "companyId"))
                .category(NotificationCategory.ISSUE)
                .channels(List.of(NotificationType.IN_APP, NotificationType.EMAIL))
                .data(event)
                .build();

        notificationService.processEvent(notification);

        // assignedTo yerine newAssignee kullan
        String assignedTo = getStringValue(event, "newAssignee");
        if (assignedTo != null && !assignedTo.isEmpty() && !assignedTo.equals(userId)) {
            NotificationEventDTO assignedNotification = NotificationEventDTO.builder()
                    .eventType("issue.assigned")
                    .userId(assignedTo)
                    .companyId(getStringValue(event, "companyId"))
                    .category(NotificationCategory.ISSUE)
                    .channels(List.of(NotificationType.IN_APP, NotificationType.PUSH))
                    .data(event)
                    .build();

            notificationService.processEvent(assignedNotification);
        }
    }

    @KafkaListener(topics = "issue.assigned", groupId = "notification-service")
    public void handleIssueAssigned(@Payload Map<String, Object> event) {
        log.info("Received issue.assigned event: {}", event);

        // assignedTo yerine newAssignee kullan
        String assignedTo = getStringValue(event, "newAssignee");
        if (assignedTo == null || assignedTo.isEmpty()) {
            log.warn("No newAssignee in issue.assigned event");
            return;
        }

        NotificationEventDTO notification = NotificationEventDTO.builder()
                .eventType("issue.assigned")
                .userId(assignedTo)
                .companyId(getStringValue(event, "companyId"))
                .category(NotificationCategory.ISSUE)
                .channels(List.of(NotificationType.IN_APP, NotificationType.PUSH, NotificationType.EMAIL))
                .data(event)
                .build();

        notificationService.processEvent(notification);
    }

    @KafkaListener(topics = "issue.updated", groupId = "notification-service")
    public void handleIssueUpdated(@Payload Map<String, Object> event) {
        log.info("Received issue.updated event: {}", event);

        // newAssignee kullan
        String assignedTo = getStringValue(event, "newAssignee");
        if (assignedTo != null && !assignedTo.isEmpty()) {
            NotificationEventDTO notification = NotificationEventDTO.builder()
                    .eventType("issue.updated")
                    .userId(assignedTo)
                    .companyId(getStringValue(event, "companyId"))
                    .category(NotificationCategory.ISSUE)
                    .channels(List.of(NotificationType.IN_APP))
                    .data(event)
                    .build();

            notificationService.processEvent(notification);
        }
    }

    @KafkaListener(topics = "issue.resolved", groupId = "notification-service")
    public void handleIssueResolved(@Payload Map<String, Object> event) {
        log.info("Received issue.resolved event: {}", event);

        // Reporter'a bildirim gönder
        String reportedBy = getStringValue(event, "reportedBy");
        if (reportedBy != null) {
            NotificationEventDTO notification = NotificationEventDTO.builder()
                    .eventType("issue.resolved")
                    .userId(reportedBy)
                    .companyId(getStringValue(event, "companyId"))
                    .category(NotificationCategory.ISSUE)
                    .channels(List.of(NotificationType.IN_APP, NotificationType.EMAIL))
                    .data(event)
                    .build();

            notificationService.processEvent(notification);
        }
    }

    @KafkaListener(topics = "issue.closed", groupId = "notification-service")
    public void handleIssueClosed(@Payload Map<String, Object> event) {
        log.info("Received issue.closed event: {}", event);

        List<String> userIds = new ArrayList<>();
        addIfNotNull(userIds, getStringValue(event, "reportedBy"));
        addIfNotNull(userIds, getStringValue(event, "assignedTo"));

        if (!userIds.isEmpty()) {
            NotificationEventDTO notification = NotificationEventDTO.builder()
                    .eventType("issue.closed")
                    .userIds(userIds)
                    .companyId(getStringValue(event, "companyId"))
                    .category(NotificationCategory.ISSUE)
                    .channels(List.of(NotificationType.IN_APP))
                    .data(event)
                    .build();

            notificationService.processEvent(notification);
        }
    }

    // ============ USER EVENTS ============

    @KafkaListener(topics = "user.registered", groupId = "notification-service")
    public void handleUserRegistered(@Payload Map<String, Object> event) {
        log.info("Received user.registered event: {}", event);

        NotificationEventDTO notification = NotificationEventDTO.builder()
                .eventType("user.registered")
                .userId(getStringValue(event, "userId"))
                .category(NotificationCategory.USER)
                .channels(List.of(NotificationType.EMAIL, NotificationType.IN_APP))
                .data(event)
                .build();

        notificationService.processEvent(notification);
    }

    // ============ TASK EVENTS ============

    @KafkaListener(topics = "task.completed", groupId = "notification-service")
    public void handleTaskCompleted(@Payload Map<String, Object> event) {
        log.info("Received task.completed event: {}", event);

        NotificationEventDTO notification = NotificationEventDTO.builder()
                .eventType("task.completed")
                .userIds(extractUserIds(event))
                .category(NotificationCategory.TASK)
                .channels(List.of(NotificationType.PUSH, NotificationType.IN_APP))
                .data(event)
                .build();

        notificationService.processEvent(notification);
    }

    // ============ PROJECT EVENTS ============

    @KafkaListener(topics = "project.budget.exceeded", groupId = "notification-service")
    public void handleBudgetExceeded(@Payload Map<String, Object> event) {
        log.info("Received project.budget.exceeded event: {}", event);

        NotificationEventDTO notification = NotificationEventDTO.builder()
                .eventType("project.budget.exceeded")
                .userIds(extractUserIds(event))
                .category(NotificationCategory.PROJECT)
                .channels(List.of(NotificationType.EMAIL, NotificationType.PUSH, NotificationType.IN_APP))
                .data(event)
                .build();

        notificationService.processEvent(notification);
    }

    // ============ HELPER METHODS ============

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private void addIfNotNull(List<String> list, String value) {
        if (value != null && !value.isEmpty()) {
            list.add(value);
        }
    }

    private List<String> extractUserIds(Map<String, Object> event) {
        Object userIds = event.get("userIds");
        if (userIds instanceof List) {
            return (List<String>) userIds;
        }

        String userId = getStringValue(event, "userId");
        return userId != null ? List.of(userId) : Collections.emptyList();
    }
}