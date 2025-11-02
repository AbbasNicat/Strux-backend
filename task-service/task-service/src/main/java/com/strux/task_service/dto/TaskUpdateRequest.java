package com.strux.task_service.dto;

import com.strux.task_service.enums.TaskCategory;
import com.strux.task_service.enums.TaskPriority;
import com.strux.task_service.enums.TaskStatus;
import com.strux.task_service.enums.TaskType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateRequest {

    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    private String description;

    private TaskStatus status;
    private TaskPriority priority;
    private TaskType type;
    private TaskCategory category;
    private String assignedTo;
    private Integer estimatedHours;
    private Integer actualHours;
    private Integer progressPercentage;
    private List<String> assignees;
    private String parentTaskId;  // Esas task -> diger tasklar

    private List<String> dependsOn;

    private String assetId;
    private String equipmentId;
    private String locationId;

    private List<String> attachmentIds;
    private List<String> tags;

    private Boolean isRecurring;
    private String recurrencePattern;  // daily, weekly, monthly

    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
