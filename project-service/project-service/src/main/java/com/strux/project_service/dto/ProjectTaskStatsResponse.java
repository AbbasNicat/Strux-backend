package com.strux.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectTaskStatsResponse {
    private Long total;
    private Long todo;
    private Long inProgress;
    private Long completed;
}

