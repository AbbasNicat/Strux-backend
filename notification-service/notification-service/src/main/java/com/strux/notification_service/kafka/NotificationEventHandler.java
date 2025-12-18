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

    // =========================
    // ISSUE EVENTS
    // =========================

    @KafkaListener(topics = "issue.created", groupId = "notification-service")
    public void handleIssueCreated(@Payload Map<String, Object> event) {
        log.info("Received issue.created event: {}", event);

        String reporter = getStringValue(event, "userId");
        if (isBlank(reporter)) {
            log.warn("No userId in issue.created event, skipping reporter notification");
        } else {
            Map<String, Object> issueData = enrichWithEventType(event, "issue.created");
            notifyOne(
                    "issue.created",
                    reporter,
                    getStringValue(event, "companyId"),
                    NotificationCategory.ISSUE,
                    List.of(NotificationType.IN_APP, NotificationType.EMAIL),
                    issueData
            );
        }

        String assignedTo = firstNonBlank(
                getStringValue(event, "newAssignee"),
                getStringValue(event, "assignedTo")
        );

        if (notBlank(assignedTo) && !assignedTo.equals(reporter)) {
            Map<String, Object> assignData = enrichWithEventType(event, "issue.assigned");
            notifyOne(
                    "issue.assigned",
                    assignedTo,
                    getStringValue(event, "companyId"),
                    NotificationCategory.ISSUE,
                    List.of(NotificationType.IN_APP, NotificationType.PUSH),
                    assignData
            );
        }
    }

    @KafkaListener(topics = "issue.assigned", groupId = "notification-service")
    public void handleIssueAssigned(@Payload Map<String, Object> event) {
        log.info("Received issue.assigned event: {}", event);

        String assignedTo = firstNonBlank(
                getStringValue(event, "newAssignee"),
                getStringValue(event, "assignedTo")
        );
        if (isBlank(assignedTo)) {
            log.warn("No assignee (newAssignee/assignedTo) in issue.assigned event");
            return;
        }

        Map<String, Object> issueData = enrichWithEventType(event, "issue.assigned");
        notifyOne(
                "issue.assigned",
                assignedTo,
                getStringValue(event, "companyId"),
                NotificationCategory.ISSUE,
                List.of(NotificationType.IN_APP, NotificationType.PUSH, NotificationType.EMAIL),
                issueData
        );
    }

    @KafkaListener(topics = "issue.updated", groupId = "notification-service")
    public void handleIssueUpdated(@Payload Map<String, Object> event) {
        log.info("Received issue.updated event: {}", event);

        String assignedTo = firstNonBlank(
                getStringValue(event, "newAssignee"),
                getStringValue(event, "assignedTo")
        );
        if (notBlank(assignedTo)) {
            Map<String, Object> issueData = enrichWithEventType(event, "issue.updated");
            notifyOne(
                    "issue.updated",
                    assignedTo,
                    getStringValue(event, "companyId"),
                    NotificationCategory.ISSUE,
                    List.of(NotificationType.IN_APP),
                    issueData
            );
        }
    }

    @KafkaListener(topics = "issue.resolved", groupId = "notification-service")
    public void handleIssueResolved(@Payload Map<String, Object> event) {
        log.info("Received issue.resolved event: {}", event);

        String reportedBy = getStringValue(event, "reportedBy");
        if (notBlank(reportedBy)) {
            Map<String, Object> issueData = enrichWithEventType(event, "issue.resolved");
            notifyOne(
                    "issue.resolved",
                    reportedBy,
                    getStringValue(event, "companyId"),
                    NotificationCategory.ISSUE,
                    List.of(NotificationType.IN_APP, NotificationType.EMAIL),
                    issueData
            );
        }
    }

    @KafkaListener(topics = "issue.closed", groupId = "notification-service")
    public void handleIssueClosed(@Payload Map<String, Object> event) {
        log.info("Received issue.closed event: {}", event);

        List<String> recipients = new ArrayList<>();
        addIfNotBlank(recipients, getStringValue(event, "reportedBy"));
        addIfNotBlank(recipients, firstNonBlank(
                getStringValue(event, "assignedTo"),
                getStringValue(event, "newAssignee")
        ));

        if (!recipients.isEmpty()) {
            Map<String, Object> issueData = enrichWithEventType(event, "issue.closed");
            notifyMany(
                    "issue.closed",
                    recipients,
                    getStringValue(event, "companyId"),
                    NotificationCategory.ISSUE,
                    List.of(NotificationType.IN_APP),
                    issueData
            );
        }
    }

    // =========================
    // USER EVENTS
    // =========================

    @KafkaListener(topics = "user.registered", groupId = "notification-service")
    public void handleUserRegistered(@Payload Map<String, Object> event) {
        log.info("Received user.registered event: {}", event);

        String userId = getStringValue(event, "userId");
        if (isBlank(userId)) {
            log.warn("user.registered without userId");
            return;
        }

        Map<String, Object> userData = enrichWithEventType(event, "user.registered");
        notifyOne(
                "user.registered",
                userId,
                getStringValue(event, "companyId"),
                NotificationCategory.USER,
                List.of(NotificationType.EMAIL, NotificationType.IN_APP),
                userData
        );
    }

    // =========================
    // TASK EVENTS
    // =========================

    @KafkaListener(topics = "task.created", groupId = "notification-service")
    public void handleTaskCreated(@Payload Map<String, Object> event) {
        log.info("Received task.created event: {}", event);

        List<String> assignees = extractRecipients(event, "newAssignees", "assignees", "assignedTo", "assigneeIds");
        String createdBy = getStringValue(event, "createdBy");
        String companyId  = getStringValue(event, "companyId");

        Map<String, Object> taskData = minimalTaskData(event);
        taskData.put("eventType", "task.created");

        if (!assignees.isEmpty()) {
            notifyMany(
                    "task.created",
                    assignees,
                    companyId,
                    NotificationCategory.TASK,
                    List.of(NotificationType.IN_APP, NotificationType.PUSH),
                    taskData
            );
        } else if (notBlank(createdBy)) {
            notifyOne(
                    "task.created",
                    createdBy,
                    companyId,
                    NotificationCategory.TASK,
                    List.of(NotificationType.IN_APP),
                    taskData
            );
        } else {
            log.warn("task.created has no assignees and no createdBy");
        }
    }

    @KafkaListener(topics = "task.assigned", groupId = "notification-service")
    public void handleTaskAssigned(@Payload Map<String, Object> event) {
        log.info("Received task.assigned event: {}", event);

        List<String> newAssignees      = extractRecipients(event, "newAssignees", "assignees", "assignedTo", "assigneeIds");
        List<String> previousAssignees = extractRecipients(event, "previousAssignees", "oldAssignees");
        String createdBy = getStringValue(event, "companyId");
        String companyId = getStringValue(event, "companyId");

        Map<String, Object> taskData = minimalTaskData(event);
        taskData.put("eventType", "task.assigned");

        if (!newAssignees.isEmpty()) {
            for (String a : newAssignees) {
                if (previousAssignees.isEmpty() || !previousAssignees.contains(a)) {
                    notifyOne(
                            "task.assigned",
                            a,
                            companyId,
                            NotificationCategory.TASK,
                            List.of(NotificationType.IN_APP, NotificationType.PUSH, NotificationType.EMAIL),
                            taskData
                    );
                }
            }
        }

        if (notBlank(createdBy) && !newAssignees.isEmpty()) {
            Map<String, Object> confirmData = new HashMap<>(taskData);
            confirmData.put("eventType", "task.assigned.confirmation");

            notifyOne(
                    "task.assigned.confirmation",
                    createdBy,
                    companyId,
                    NotificationCategory.TASK,
                    List.of(NotificationType.IN_APP),
                    confirmData
            );
        }
    }

    @KafkaListener(topics = "task.completed", groupId = "notification-service")
    public void handleTaskCompleted(@Payload Map<String, Object> event) {
        log.info("Received task.completed event: {}", event);

        String completedBy = getStringValue(event, "completedBy");
        String createdBy   = getStringValue(event, "createdBy");
        String companyId   = getStringValue(event, "companyId");

        // Worker'a onay bildirimi
        if (notBlank(completedBy)) {
            Map<String, Object> workerData = minimalTaskData(event);
            workerData.put("eventType", "task.completed.worker.confirmation");

            notifyOne(
                    "task.completed.worker.confirmation",
                    completedBy,
                    companyId,
                    NotificationCategory.TASK,
                    List.of(NotificationType.IN_APP),
                    workerData
            );
        }

        // Admin'e approval bekliyor bildirimi
        if (notBlank(createdBy)) {
            Map<String, Object> adminData = minimalTaskData(event);
            adminData.put("eventType", "task.completed.pending.approval");
            adminData.put("completedBy", completedBy);
            adminData.put("requiresApproval", true);

            notifyOne(
                    "task.completed.pending.approval",
                    createdBy,
                    companyId,
                    NotificationCategory.TASK,
                    List.of(NotificationType.IN_APP, NotificationType.PUSH, NotificationType.EMAIL),
                    adminData
            );
        }
    }

    @KafkaListener(topics = "task.approved", groupId = "notification-service")
    public void handleTaskApproved(@Payload Map<String, Object> event) {
        log.info("Received task.approved event: {}", event);

        String approvedBy = getStringValue(event, "approvedBy");
        String completedBy = getStringValue(event, "completedBy");
        List<String> assignees = extractRecipients(event, "assignees", "assignedTo", "assigneeIds");
        String companyId = getStringValue(event, "companyId");

        Map<String, Object> taskData = minimalTaskData(event);
        taskData.put("eventType", "task.approved");

        if (!assignees.isEmpty()) {
            notifyMany(
                    "task.approved",
                    assignees,
                    companyId,
                    NotificationCategory.TASK,
                    List.of(NotificationType.IN_APP, NotificationType.PUSH),
                    taskData
            );
        }
    }

    @KafkaListener(topics = "task.rejected", groupId = "notification-service")
    public void handleTaskRejected(@Payload Map<String, Object> event) {
        log.info("Received task.rejected event: {}", event);

        String rejectedBy = getStringValue(event, "rejectedBy");
        String rejectionReason = getStringValue(event, "rejectionReason");
        List<String> assignees = extractRecipients(event, "assignees", "assignedTo", "assigneeIds");
        String companyId = getStringValue(event, "companyId");

        Map<String, Object> rejectData = minimalTaskData(event);
        rejectData.put("eventType", "task.rejected");
        rejectData.put("rejectionReason", rejectionReason);
        rejectData.put("rejectedBy", rejectedBy);

        if (!assignees.isEmpty()) {
            notifyMany(
                    "task.rejected",
                    assignees,
                    companyId,
                    NotificationCategory.TASK,
                    List.of(NotificationType.IN_APP, NotificationType.PUSH, NotificationType.EMAIL),
                    rejectData
            );
        }
    }

    @KafkaListener(topics = "task.status.changed", groupId = "notification-service")
    public void handleTaskStatusChanged(@Payload Map<String, Object> event) {
        log.info("Received task.status.changed event: {}", event);

        String newStatus = getStringValue(event, "newStatus");
        String oldStatus = getStringValue(event, "oldStatus");
        String createdBy = getStringValue(event, "createdBy");
        String updatedBy = getStringValue(event, "updatedBy");
        List<String> assignees = extractRecipients(event, "assignees", "assignedTo", "assigneeIds", "newAssignees");
        String companyId = getStringValue(event, "companyId");

        // Worker task'ı PENDING_APPROVAL yapınca admin'e notification
        if ("PENDING_APPROVAL".equals(newStatus) && notBlank(createdBy)) {
            Map<String, Object> approvalData = minimalTaskData(event);
            approvalData.put("eventType", "task.completed.pending.approval");
            approvalData.put("completedBy", updatedBy);
            approvalData.put("requiresApproval", true);

            notifyOne(
                    "task.completed.pending.approval",
                    createdBy,
                    companyId,
                    NotificationCategory.TASK,
                    List.of(NotificationType.IN_APP, NotificationType.PUSH, NotificationType.EMAIL),
                    approvalData
            );

            log.info("Admin notification sent for PENDING_APPROVAL task: {}", event.get("taskId"));
        }

        // Önemli status değişiklikleri
        if ("IN_PROGRESS".equals(newStatus) || "ON_HOLD".equals(newStatus) ||
                "CANCELLED".equals(newStatus) || "COMPLETED".equals(newStatus)) {

            Map<String, Object> statusData = minimalTaskData(event);
            statusData.put("eventType", "task.status.changed");
            statusData.put("newStatus", newStatus);
            statusData.put("oldStatus", oldStatus);

            // Admin'e status değişikliği bildirimi
            if (notBlank(createdBy) && notBlank(updatedBy) && !createdBy.equals(updatedBy)) {
                notifyOne(
                        "task.status.changed",
                        createdBy,
                        companyId,
                        NotificationCategory.TASK,
                        List.of(NotificationType.IN_APP, NotificationType.PUSH),
                        statusData
                );
            }

            // Worker'lara status değişikliği bildirimi
            if (!assignees.isEmpty() && notBlank(updatedBy)) {
                List<String> recipientsExcludingUpdater = assignees.stream()
                        .filter(a -> !a.equals(updatedBy))
                        .toList();

                if (!recipientsExcludingUpdater.isEmpty()) {
                    notifyMany(
                            "task.status.changed",
                            recipientsExcludingUpdater,
                            companyId,
                            NotificationCategory.TASK,
                            List.of(NotificationType.IN_APP, NotificationType.PUSH),
                            statusData
                    );
                }
            }
        }
    }

    @KafkaListener(topics = "task.progress.updated", groupId = "notification-service")
    public void handleTaskProgressUpdated(@Payload Map<String, Object> event) {
        log.info("Received task.progress.updated event: {}", event);

        Integer pct = getIntegerValue(event, "progressPercentage");
        if (pct == null) return;

        String companyId = getStringValue(event, "companyId");
        String createdBy = getStringValue(event, "createdBy");
        String updatedBy = getStringValue(event, "updatedBy");
        List<String> assignees = extractRecipients(event, "assignees", "assignedTo", "assigneeIds", "newAssignees");

        Map<String, Object> progressData = minimalTaskData(event);
        progressData.put("eventType", "task.progress.updated");

        // Admin'e bildirim
        if (notBlank(createdBy) && notBlank(updatedBy) && !createdBy.equals(updatedBy)) {
            notifyOne(
                    "task.progress.updated",
                    createdBy,
                    companyId,
                    NotificationCategory.TASK,
                    List.of(NotificationType.IN_APP),
                    progressData
            );
        }

        // Milestone notification (25%, 50%, 75%, 100%)
        if (pct == 25 || pct == 50 || pct == 75 || pct == 100) {
            Map<String, Object> milestoneData = minimalTaskData(event);
            milestoneData.put("eventType", "task.progress.milestone");

            if (notBlank(createdBy)) {
                notifyOne(
                        "task.progress.milestone",
                        createdBy,
                        companyId,
                        NotificationCategory.TASK,
                        List.of(NotificationType.IN_APP, NotificationType.EMAIL),
                        milestoneData
                );
            }

            if (!assignees.isEmpty()) {
                notifyMany(
                        "task.progress.milestone",
                        assignees,
                        companyId,
                        NotificationCategory.TASK,
                        List.of(NotificationType.IN_APP),
                        milestoneData
                );
            }
        }
    }

    // =========================
    // PROJECT EVENTS
    // =========================

    @KafkaListener(topics = "project.budget.exceeded", groupId = "notification-service")
    public void handleBudgetExceeded(@Payload Map<String, Object> event) {
        log.info("Received project.budget.exceeded event: {}", event);

        List<String> recipients = extractUserIds(event);
        if (recipients.isEmpty()) {
            log.warn("project.budget.exceeded has no recipients");
            return;
        }

        Map<String, Object> projectData = enrichWithEventType(event, "project.budget.exceeded");
        notifyMany(
                "project.budget.exceeded",
                recipients,
                getStringValue(event, "companyId"),
                NotificationCategory.PROJECT,
                List.of(NotificationType.EMAIL, NotificationType.PUSH, NotificationType.IN_APP),
                projectData
        );
    }

    // =========================
    // WORKER EVENTS
    // =========================

    @KafkaListener(topics = "worker.assigned.to.unit", groupId = "notification-service")
    public void handleWorkerAssignedToUnit(@Payload Map<String, Object> event) {
        log.info("📨 Received worker.assigned.to.unit event: {}", event);

        String workerId = getStringValue(event, "workerId");
        String workerName = getStringValue(event, "workerName");           // ✅ NEW
        String unitId = getStringValue(event, "unitId");
        String unitName = getStringValue(event, "unitName");
        String assignedBy = getStringValue(event, "assignedBy");
        String assignerName = getStringValue(event, "assignerName");       // ✅ NEW
        String companyId = getStringValue(event, "companyId");
        String projectId = getStringValue(event, "projectId");

        if (isBlank(workerId)) {
            log.warn("worker.assigned.to.unit without workerId");
            return;
        }

        // ✅ Build notification data with ALL names
        Map<String, Object> notifData = new HashMap<>();
        notifData.put("eventType", "worker.assigned.to.unit");
        notifData.put("workerId", workerId);
        notifData.put("workerName", workerName);                          // ✅
        notifData.put("unitId", unitId);
        notifData.put("unitName", unitName);
        notifData.put("assignedBy", assignedBy);
        notifData.put("assignerName", assignerName);                      // ✅
        notifData.put("projectId", projectId);

        // 1️⃣ Worker'a bildirim (with names in message)
        notifyOne(
                "worker.assigned.to.unit",
                workerId,
                companyId,
                NotificationCategory.WORKER,
                List.of(NotificationType.IN_APP, NotificationType.PUSH),
                notifData
        );

        log.info("✅ Sent worker.assigned.to.unit notification to worker: {} ({})",
                workerId, workerName);

        // 2️⃣ Atayan kişiye confirmation (with names)
        if (notBlank(assignedBy) && !assignedBy.equals(workerId)) {
            Map<String, Object> confirmData = new HashMap<>(notifData);
            confirmData.put("eventType", "worker.assigned.to.unit.confirmation");

            notifyOne(
                    "worker.assigned.to.unit.confirmation",
                    assignedBy,
                    companyId,
                    NotificationCategory.WORKER,
                    List.of(NotificationType.IN_APP),
                    confirmData
            );

            log.info("✅ Sent worker.assigned.to.unit.confirmation to assigner: {} ({})",
                    assignedBy, assignerName);
        }
    }

    @KafkaListener(topics = "worker.removed.from.unit", groupId = "notification-service")
    public void handleWorkerRemovedFromUnit(@Payload Map<String, Object> event) {
        log.info("📨 Received worker.removed.from.unit event: {}", event);

        String workerId = getStringValue(event, "workerId");
        String workerName = getStringValue(event, "workerName");           // ✅ NEW
        String unitId = getStringValue(event, "unitId");
        String unitName = getStringValue(event, "unitName");
        String removedBy = getStringValue(event, "removedBy");
        String removerName = getStringValue(event, "removerName");         // ✅ NEW
        String reason = getStringValue(event, "reason");
        String companyId = getStringValue(event, "companyId");

        if (isBlank(workerId)) {
            log.warn("worker.removed.from.unit without workerId");
            return;
        }

        // ✅ Build notification data with ALL names
        Map<String, Object> notifData = new HashMap<>();
        notifData.put("eventType", "worker.removed.from.unit");
        notifData.put("workerId", workerId);
        notifData.put("workerName", workerName);                          // ✅
        notifData.put("unitId", unitId);
        notifData.put("unitName", unitName);
        notifData.put("removedBy", removedBy);
        notifData.put("removerName", removerName);                        // ✅
        notifData.put("reason", reason);

        // 1️⃣ Worker'a bildirim (with names in message)
        notifyOne(
                "worker.removed.from.unit",
                workerId,
                companyId,
                NotificationCategory.WORKER,
                List.of(NotificationType.IN_APP, NotificationType.PUSH),
                notifData
        );

        log.info("✅ Sent worker.removed.from.unit notification to worker: {} ({})",
                workerId, workerName);

        // 2️⃣ Çıkaran kişiye confirmation (with names)
        if (notBlank(removedBy) && !removedBy.equals(workerId)) {
            Map<String, Object> confirmData = new HashMap<>(notifData);
            confirmData.put("eventType", "worker.removed.from.unit.confirmation");

            notifyOne(
                    "worker.removed.from.unit.confirmation",
                    removedBy,
                    companyId,
                    NotificationCategory.WORKER,
                    List.of(NotificationType.IN_APP),
                    confirmData
            );

            log.info("✅ Sent worker.removed.from.unit.confirmation to remover: {} ({})",
                    removedBy, removerName);
        }
    }

    // =========================
    // HELPERS
    // =========================

    private void notifyOne(String eventType,
                           String rawUserId,
                           String companyId,
                           NotificationCategory category,
                           List<NotificationType> channels,
                           Map<String, Object> data) {

        if (isBlank(rawUserId)) return;

        NotificationEventDTO dto = NotificationEventDTO.builder()
                .eventType(eventType)
                .userId(rawUserId)
                .companyId(companyId)
                .category(category)
                .channels(channels)
                .data(data != null ? data : Collections.emptyMap())
                .build();

        notificationService.processEvent(dto);
    }

    private void notifyMany(String eventType,
                            List<String> rawUserIds,
                            String companyId,
                            NotificationCategory category,
                            List<NotificationType> channels,
                            Map<String, Object> data) {

        for (String uid : rawUserIds) {
            notifyOne(eventType, uid, companyId, category, channels, data);
        }
    }

    private Map<String, Object> minimalTaskData(Map<String, Object> event) {
        String taskId = getStringValue(event, "taskId");
        String title  = getStringValue(event, "title");
        Integer progressPercentage = getIntegerValue(event, "progressPercentage");
        String newStatus = getStringValue(event, "newStatus");
        String oldStatus = getStringValue(event, "oldStatus");

        Map<String, Object> map = new HashMap<>();
        if (taskId != null) map.put("taskId", taskId);
        if (title  != null) map.put("title",  title);
        if (progressPercentage != null) map.put("progressPercentage", progressPercentage);
        if (newStatus != null) map.put("newStatus", newStatus);
        if (oldStatus != null) map.put("oldStatus", oldStatus);

        return map;
    }

    private Map<String, Object> enrichWithEventType(Map<String, Object> originalEvent, String eventType) {
        Map<String, Object> enriched = new HashMap<>(originalEvent);
        enriched.put("eventType", eventType);
        return enriched;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return null; }
    }

    private List<String> extractRecipients(Map<String, Object> event, String... keys) {
        return getStringList(event, keys);
    }

    private List<String> extractUserIds(Map<String, Object> event) {
        Object userIds = event.get("userIds");
        if (userIds instanceof List<?> l) {
            List<String> out = new ArrayList<>();
            for (Object o : l) if (o != null && notBlank(o.toString())) out.add(o.toString());
            if (!out.isEmpty()) return out;
        }
        String userId = getStringValue(event, "userId");
        return notBlank(userId) ? List.of(userId) : Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<String> getStringList(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object v = map.get(key);
            if (v == null) continue;

            if (v instanceof List<?>) {
                List<?> raw = (List<?>) v;
                List<String> out = new ArrayList<>();
                for (Object o : raw) if (o != null && notBlank(o.toString())) out.add(o.toString());
                if (!out.isEmpty()) return out;
            } else {
                String s = v.toString().trim();
                if (s.isEmpty()) continue;
                if (s.contains(",")) {
                    List<String> out = new ArrayList<>();
                    for (String part : s.split(",")) {
                        String p = part.trim();
                        if (!p.isEmpty()) out.add(p);
                    }
                    if (!out.isEmpty()) return out;
                } else {
                    return List.of(s);
                }
            }
        }
        return Collections.emptyList();
    }

    private void addIfNotBlank(List<String> list, String value) {
        if (notBlank(value)) list.add(value);
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String firstNonBlank(String... vals) {
        if (vals == null) return null;
        for (String v : vals) if (notBlank(v)) return v;
        return null;
    }
}