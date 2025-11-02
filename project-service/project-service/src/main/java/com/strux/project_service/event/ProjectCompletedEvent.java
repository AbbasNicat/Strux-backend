package com.strux.project_service.event;

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
public class ProjectCompletedEvent {
    private String projectId;
    private String companyId;
    private String projectName;
    private LocalDate startDate;
    private LocalDate plannedEndDate;
    private LocalDate completionDate;
    private Integer daysAheadOrBehind;
    private BigDecimal budget;
    private BigDecimal finalCost;
    private Integer totalUnits;
    private Integer completedUnits;
    private Integer totalTasks;
    private Integer completedTasks;
    private LocalDateTime timestamp;
}
