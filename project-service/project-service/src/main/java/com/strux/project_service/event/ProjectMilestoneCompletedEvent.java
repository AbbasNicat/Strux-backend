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
public class ProjectMilestoneCompletedEvent {
    private String projectId;
    private String companyId;
    private String milestoneName;
    private String milestoneDescription;
    private Integer targetPercentage; // örn: 25, 50, 75
    private BigDecimal actualProgress;
    private LocalDate plannedDate;
    private LocalDate completionDate;
    private Integer daysAheadOrBehind; // +5 (ahead), -3 (behind)
    private LocalDateTime timestamp;
}
