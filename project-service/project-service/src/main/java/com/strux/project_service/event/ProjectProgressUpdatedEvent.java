package com.strux.project_service.event;

import lombok.Data;
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
public class ProjectProgressUpdatedEvent {
    private String projectId;
    private String companyId;
    private BigDecimal previousProgress;
    private BigDecimal currentProgress;
    private BigDecimal progressChange;
    private Integer completedTasks;
    private Integer totalTasks;
    private Integer completedUnits;
    private Integer totalUnits;
    private LocalDateTime timestamp;
}
