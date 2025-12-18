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
    private String footprintJson;


    private Long availableUnits;
    private Long reservedUnits;
    private Long soldUnits;

    // ========== HIERARCHICAL STATS ==========
    private Long totalBuildings;  // ✅ Total number of buildings
    private Long totalApartments;  // ✅ Total number of apartments
    private Long standAloneUnits;  // ✅ Units without parent
    private Long unitsWithSubUnits;  // ✅ Buildings containing apartments

    // ========== FLOOR PLAN STATS ==========
    private Long unitsWithFloorPlan;  // ✅ Units that have floor plan
    private Long unitsWithoutFloorPlan;  // ✅ Units without floor plan
    private Double averageFloorPlanArea;  // ✅ Average floor plan area (width x length)
    private Double averageCeilingHeight;  // ✅ Average ceiling height

    private Map<String, Long> unitsByType;
    private Map<String, Long> unitsByStatus;
    private Map<String, Long> unitsBySaleStatus;
    private Map<String, Long> unitsByPhase;

    // ========== HIERARCHICAL BREAKDOWN ==========
    private Map<String, Long> apartmentsByBuilding;  // ✅ Apartments grouped by parent building

    private Double averageCompletionPercentage;
    private Double totalGrossArea;
    private Double totalNetArea;

    private BigDecimal totalSalesValue;
    private BigDecimal totalCollectedPayments;
    private BigDecimal totalRemainingPayments;

    private Integer averageQualityScore;
}