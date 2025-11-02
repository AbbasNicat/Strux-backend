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
        event.put("companyId", task.getCompanyId());
        event.put("projectId", task.getProjectId());
        event.put("previousAssignees", previousAssignees);
        event.put("newAssignees", task.getAssignees());
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send("task.assigned", event);
        log.info("Task assigned event published: {}", task.getId());
    }

    public void publishTaskStatusChangedEvent(Task task, TaskStatus oldStatus) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "task.status.changed");
        event.put("taskId", task.getId());
        event.put("companyId", task.getCompanyId());
        event.put("projectId", task.getProjectId());
        event.put("oldStatus", oldStatus);
        event.put("newStatus", task.getStatus());
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send("task.status.changed", event);
        log.info("Task status changed event published: {} -> {}", oldStatus, task.getStatus());
    }

    public void publishTaskProgressUpdatedEvent(Task task) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "task.progress.updated");
        event.put("taskId", task.getId());
        event.put("companyId", task.getCompanyId());
        event.put("projectId", task.getProjectId());
        event.put("progressPercentage", task.getProgressPercentage());
        event.put("actualHours", task.getActualHours());
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send("task.progress.updated", event);
        log.info("Task progress updated event published: {}%", task.getProgressPercentage());
    }

    public void publishTaskCompletedEvent(Task task) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "task.completed");
        event.put("taskId", task.getId());
        event.put("companyId", task.getCompanyId());
        event.put("projectId", task.getProjectId());
        event.put("completedAt", task.getCompletedAt());
        event.put("assignedTo", task.getAssignedTo());
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send("task.completed", event);
        log.info("Task completed event published: {}", task.getId());
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
