package com.strux.project_service.dto;

import lombok.Data;

@Data
public class ProjectStatsDTO {
    private String projectId;
    private Integer totalWorkers;
    private Integer totalUnits;
    private Integer activeTasks;
}
