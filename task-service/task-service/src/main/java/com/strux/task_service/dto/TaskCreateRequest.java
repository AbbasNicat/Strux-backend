package com.strux.task_service.dto;

import com.strux.task_service.enums.TaskCategory;
import com.strux.task_service.enums.TaskPriority;
import com.strux.task_service.enums.TaskStatus;
import com.strux.task_service.enums.TaskType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class TaskCreateRequest {
    private String title;

    private String description;

    private String companyId;
    private String projectId;
    private String createdBy;
    private String assignedTo;

    private List<String> assignees;  // gorevi veren

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    @Enumerated(EnumType.STRING)
    private TaskType type;

    @Enumerated(EnumType.STRING)
    private TaskCategory category;

    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;

    private String unitId;


    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private Integer estimatedHours;
    private Integer actualHours;
    private Integer progressPercentage;  // 0-100

    private String parentTaskId;  // Esas task -> diger tasklar

    private List<String> dependsOn;

    private String assetId;
    private String equipmentId;
    private String locationId;

    private List<String> attachmentIds;

    private List<String> tags;

    private Boolean isRecurring;
    private String recurrencePattern;  // daily

}
