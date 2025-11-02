package com.strux.task_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatsResponse {
    private Long todoTasks;
    private Long completedTasks;
    private Long cancelledTasks;
    private Long onHoldTasks;
    private Long totalTasks;
    private Long inProgressTasks;

    private Long closedTasks;

    private Map<String, Long> tasksByCategory;
    private Map<String, Long> tasksByType;
    private Map<String, Long> tasksByPriority;
    private Map<String, Long> tasksByStatus;

    private Double averageResolutionTimeHours;
    private Long overdueTasks;
}
