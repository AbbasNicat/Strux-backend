package com.strux.project_service.event;

import com.strux.project_service.enums.ProjectStatus;
import com.strux.project_service.enums.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkerAssignedToProjectEvent {
    private String projectId;
    private String companyId;
    private String userId;
    private String workerName;
    private String position;
    private String specialty; // MASON, ELECTRICIAN, etc.
    private LocalDate assignmentDate;
    private String assignedBy;
    private LocalDateTime timestamp;
}
