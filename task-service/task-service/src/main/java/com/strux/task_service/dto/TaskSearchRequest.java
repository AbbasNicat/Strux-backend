package com.strux.task_service.dto;

import com.strux.task_service.enums.TaskCategory;
import com.strux.task_service.enums.TaskPriority;
import com.strux.task_service.enums.TaskStatus;
import com.strux.task_service.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSearchRequest {

    private String keyword;
    private String projectId;
    private String companyId;
    private String createdBy;
    private String assignedTo;
    private TaskStatus status;
    private TaskPriority priority;
    private TaskType type;
    private TaskCategory category;
    private String parentTaskId;
    private String assetId;
    private String equipmentId;
    private String locationId;
    private Boolean isRecurring;
    private Boolean isTemplate;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private LocalDateTime dueDateAfter;
    private LocalDateTime dueDateBefore;
    private Integer minProgressPercentage;
    private Integer maxProgressPercentage;
}
