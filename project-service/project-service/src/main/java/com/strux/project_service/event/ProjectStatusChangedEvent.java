package com.strux.project_service.event;

import com.strux.project_service.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectStatusChangedEvent {
    private String projectId;
    private String companyId;
    private ProjectStatus oldStatus;
    private ProjectStatus newStatus;
    private String reason;
    private String changedBy;
    private LocalDateTime timestamp;
}