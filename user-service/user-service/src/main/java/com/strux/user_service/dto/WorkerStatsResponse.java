package com.strux.user_service.dto;

import com.strux.user_service.enums.WorkerSpecialty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerStatsResponse {

    private String workerId;
    private String fullName;
    private WorkerSpecialty specialty;
    private BigDecimal rating;
    private Integer completedTasks;
    private Integer totalWorkDays;
    private Integer onTimeCompletionCount;
    private Integer lateCompletionCount;
    private BigDecimal reliabilityScore;
    private Integer activeProjectCount;
    private Boolean isAvailable;
    private Integer experienceYears;
    private List<String> certifications;
}
