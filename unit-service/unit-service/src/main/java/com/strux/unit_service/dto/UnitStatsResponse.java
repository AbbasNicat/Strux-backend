package com.strux.unit_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitStatsResponse {

    private Long totalUnits;
    private Long plannedUnits;
    private Long inConstructionUnits;
    private Long completedUnits;
    private Long deliveredUnits;

    private Long availableUnits;
    private Long reservedUnits;
    private Long soldUnits;

    private Map<String, Long> unitsByType;
    private Map<String, Long> unitsByStatus;
    private Map<String, Long> unitsBySaleStatus;
    private Map<String, Long> unitsByPhase;

    private Double averageCompletionPercentage;
    private Double totalGrossArea;
    private Double totalNetArea;

    private BigDecimal totalSalesValue;
    private BigDecimal totalCollectedPayments;
    private BigDecimal totalRemainingPayments;

    private Integer averageQualityScore;
}
