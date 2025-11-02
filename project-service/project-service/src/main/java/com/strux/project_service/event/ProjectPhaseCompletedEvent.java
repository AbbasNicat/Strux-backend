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
public class ProjectPhaseCompletedEvent {
    private String projectId;
    private String companyId;
    private String phaseId;
    private String phaseName;
    private String phaseDescription;
    private LocalDate plannedEndDate;
    private LocalDate completionDate;
    private Integer daysAheadOrBehind;
    private BigDecimal phaseProgress;
    private LocalDateTime timestamp;
}

