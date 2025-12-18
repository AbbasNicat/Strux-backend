package com.strux.task_service.kafka;

import com.strux.task_service.enums.TaskStatus;
import com.strux.task_service.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTaskCreatedEvent(Task task) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "task.created");
        event.put("taskId", task.getId());
        event.put("title", task.getTitle());
        event.put("companyId", task.getCompanyId());
        event.put("projectId", task.getProjectId());
        event.put("createdBy", task.getCreatedBy());
        event.put("assignedTo", task.getAssignedTo());
        event.put("priority", task.getPriority());
        event.put("dueDate", task.getDueDate());
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send("task.created", event);
        log.info("Task created event published: {}", task.getId());
    }

    public void publishTaskUpdatedEvent(Task task) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "task.updated");
        event.put("taskId", task.getId());
        event.put("companyId", task.getCompanyId());
        event.put("projectId", task.getProjectId());
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send("task.updated", event);
        log.info("Task updated event published: {}", task.getId());
    }

    public void publishTaskAssignedEvent(Task task, List<String> previousAssignees) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "task.assigned");
        event.put("taskId", task.getId());
        event.put("title", task.getTitle());
        event.put("companyId", task.getCompanyId());
        event.put("projectId", task.getProjectId());
        event.put("previousAssignees", previousAssignees);
        event.put("newAssignees", task.getAssignees());
        event.put("createdBy", task.getCreatedBy());
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send("task.assigned", event);
        log.info("Task assigned event published: {}", task.getId());
    }

    // ✅ updatedBy parametresi eklendi
    public void publishTaskStatusChangedEvent(Task task, TaskStatus oldStatus, String updatedBy) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "task.status.changed");
        event.put("taskId", task.getId());
        event.put("title", task.getTitle());
        event.put("companyId", task.getCompanyId());
        event.put("projectId", task.getProjectId());
        event.put("oldStatus", oldStatus);
        event.put("newStatus", task.getStatus());
        event.put("createdBy", task.getCreatedBy());
        event.put("assignees", task.getAssignees());
        event.put("updatedBy", updatedBy); // ✅ EKLENDI
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send("task.status.changed", event);
        log.info("Task status changed event published: {} -> {}", oldStatus, task.getStatus());
    }

    // ✅ updatedBy parametresi eklendi
    public void publishTaskProgressUpdatedEvent(Task task, String updatedBy) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "task.progress.updated");
        event.put("taskId", task.getId());
        event.put("title", task.getTitle());
        event.put("companyId", task.getCompanyId());
        event.put("projectId", task.getProjectId());
        event.put("progressPercentage", task.getProgressPercentage());
        event.put("actualHours", task.getActualHours());
        event.put("createdBy", task.getCreatedBy()); // ✅ EKLENDI
        event.put("assignees", task.getAssignees()); // ✅ EKLENDI
        event.put("updatedBy", updatedBy); // ✅ EKLENDI
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send("task.progress.updated", event);
        log.info("Task progress updated event published: {}%", task.getProgressPercentage());
    }

    public void publishTaskCompletedEvent(Task task, String completedBy) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "task.completed");
        event.put("taskId", task.getId());
        event.put("title", task.getTitle());
        event.put("companyId", task.getCompanyId());
        event.put("projectId", task.getProjectId());
        event.put("completedAt", task.getCompletedAt());
        event.put("completedBy", completedBy);
        event.put("createdBy", task.getCreatedBy());
        event.put("assignees", task.getAssignees());
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send("task.completed", event);
        log.info("Task completed event published: {}", task.getId());
    }

    // ✅ YENİ: Task Approved Event
    public void publishTaskApprovedEvent(Task task, String approvedBy) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "task.approved");
        event.put("taskId", task.getId());
        event.put("title", task.getTitle());
        event.put("companyId", task.getCompanyId());
        event.put("projectId", task.getProjectId());
        event.put("approvedBy", approvedBy);
        event.put("completedBy", task.getAssignees() != null && !task.getAssignees().isEmpty()
                ? task.getAssignees().get(0) : null);
        event.put("assignees", task.getAssignees());
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send("task.approved", event);
        log.info("Task approved event published: {}", task.getId());
    }

    // ✅ YENİ: Task Rejected Event
    public void publishTaskRejectedEvent(Task task, String rejectedBy, String rejectionReason) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "task.rejected");
        event.put("taskId", task.getId());
        event.put("title", task.getTitle());
        event.put("companyId", task.getCompanyId());
        event.put("projectId", task.getProjectId());
        event.put("rejectedBy", rejectedBy);
        event.put("rejectionReason", rejectionReason);
        event.put("assignees", task.getAssignees());
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send("task.rejected", event);
        log.info("Task rejected event published: {}", task.getId());
    }

    public void publishTaskDeletedEvent(Task task, boolean hardDelete) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "task.deleted");
        event.put("taskId", task.getId());
        event.put("companyId", task.getCompanyId());
        event.put("projectId", task.getProjectId());
        event.put("hardDelete", hardDelete);
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send("task.deleted", event);
        log.info("Task deleted event published: {}", task.getId());
    }
}