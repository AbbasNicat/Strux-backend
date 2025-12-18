package com.strux.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectStatsResponse {
    private String projectId;
    private String name;
    private Long workerCount;
    private Long unitCount;
    private Long taskCount;
    private Integer completionPercentage;
}
