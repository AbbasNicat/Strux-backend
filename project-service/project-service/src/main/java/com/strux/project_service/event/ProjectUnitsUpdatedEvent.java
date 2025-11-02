package com.strux.project_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUnitsUpdatedEvent {
    private String projectId;
    private String companyId;
    private Integer previousTotalUnits;
    private Integer newTotalUnits;
    private Integer unitsAdded;
    private String updatedBy;
    private LocalDateTime timestamp;
}
