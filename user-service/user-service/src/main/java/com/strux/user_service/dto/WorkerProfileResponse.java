package com.strux.user_service.dto;

import com.strux.user_service.enums.WorkerSpecialty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerProfileResponse {
    private WorkerSpecialty specialty;
    private Integer experienceYears;
    private BigDecimal hourlyRate;
    private BigDecimal rating;
    private Integer completedTasks;
    private Integer totalWorkDays;
    private Integer onTimeCompletionCount;
    private Integer lateCompletionCount;
    private BigDecimal reliabilityScore;
    private List<String> activeProjectIds;
    private Boolean isAvailable;
    private LocalDate availableFrom;
}
