package com.strux.project_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AddPhaseRequest {

    private String phaseName;

    private String description;

    private BigDecimal weightPercentage;

    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;

    private Integer orderIndex;
}
