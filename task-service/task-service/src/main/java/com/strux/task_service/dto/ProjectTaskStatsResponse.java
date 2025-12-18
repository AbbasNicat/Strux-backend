package com.strux.task_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectTaskStatsResponse {
    private Long total;
    private Long todo;
    private Long inProgress;
    private Long completed;
}
