package com.strux.project_service.dto;

import com.strux.project_service.enums.PhaseStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateProgressRequest {
    @Min(0)
    @Max(100)
    private BigDecimal currentProgress;
    @Min(0)
    @Max(100)
    private BigDecimal weightPercentage;

    private PhaseStatus status;

    private LocalDate actualStartDate;
    private LocalDate actualEndDate;

    private String notes; // İşçi notları
}
