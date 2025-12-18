package com.strux.unit_service.dto;

import com.strux.unit_service.enums.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitSearchRequest {

    private String keyword;
    private String companyId;
    private String projectId;

    // ========== HIERARCHICAL SEARCH ==========
    private String parentUnitId;  // ✅ Search by parent building
    private Boolean hasSubUnits;  // ✅ Filter buildings vs apartments

    private String buildingId;  // Legacy
    private String blockName;

    private UnitType type;
    private UnitStatus status;
    private SaleStatus saleStatus;
    private ConstructionPhase currentPhase;

    private Integer minFloor;
    private Integer maxFloor;

    private BigDecimal minGrossArea;
    private BigDecimal maxGrossArea;

    private BigDecimal minNetArea;
    private BigDecimal maxNetArea;

    private Integer minRoomCount;
    private Integer maxRoomCount;

    private Integer minBedroomCount;
    private Integer maxBedroomCount;

    private Direction direction;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private Integer minCompletionPercentage;
    private Integer maxCompletionPercentage;

    private Boolean hasGarden;
    private Boolean hasTerrace;
    private Boolean isSmartHome;
    private Boolean hasParkingSpace;

    // ========== FLOOR PLAN FILTERS ==========
    private Boolean hasFloorPlan;  // ✅ Filter units with floor plan

    private BigDecimal minFloorPlanWidth;  // ✅ Min width in meters
    private BigDecimal maxFloorPlanWidth;  // ✅ Max width in meters

    private BigDecimal minFloorPlanLength;  // ✅ Min length in meters
    private BigDecimal maxFloorPlanLength;  // ✅ Max length in meters

    private BigDecimal minCeilingHeight;  // ✅ Min ceiling height
    private BigDecimal maxCeilingHeight;  // ✅ Max ceiling height

    private List<String> tags;

    private LocalDateTime constructionStartAfter;
    private LocalDateTime constructionStartBefore;
    private LocalDateTime estimatedCompletionAfter;
    private LocalDateTime estimatedCompletionBefore;
}