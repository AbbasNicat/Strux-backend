package com.strux.unit_service.dto;

import com.strux.unit_service.enums.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitCreateRequest {

    @NotBlank(message = "Unit number is required")
    private String unitNumber;

    private String unitName;
    private String description;
    private String footprintJson;


    @NotBlank(message = "Company ID is required")
    private String companyId;

    @NotBlank(message = "Project ID is required")
    private String projectId;

    // ========== HIERARCHICAL STRUCTURE ==========
    private String parentUnitId;  // ✅ If creating apartment under building
    private Boolean hasSubUnits;  // ✅ If creating building that will have apartments

    // Legacy (deprecated)
    @Deprecated
    private String buildingId;

    private String blockName;
    private Integer floor;
    private String section;

    @NotNull(message = "Unit type is required")
    private UnitType type;

    // ========== DIMENSIONS ==========
    private BigDecimal grossArea;
    private BigDecimal netArea;
    private Integer roomCount;
    private Integer bedroomCount;
    private Integer bathroomCount;
    private Integer balconyCount;
    private Direction direction;

    private Boolean hasGarden;
    private BigDecimal gardenArea;
    private Boolean hasTerrace;
    private BigDecimal terraceArea;

    // ========== FLOOR PLAN DATA ==========
    private String floorPlanJson;  // ✅ Fabric.js canvas JSON
    private String floorPlanImageUrl;  // ✅ PNG export URL
    private BigDecimal floorPlanWidth;  // ✅ Width in meters
    private BigDecimal floorPlanLength;  // ✅ Length in meters
    private BigDecimal ceilingHeight;  // ✅ Height in meters

    // ========== CONSTRUCTION ==========
    private UnitStatus status;
    private ConstructionPhase currentPhase;
    private LocalDateTime constructionStartDate;
    private LocalDateTime estimatedCompletionDate;
    private LocalDateTime actualCompletionDate;

    // ========== SALE ==========
    private SaleStatus saleStatus;
    private BigDecimal listPrice;
    private BigDecimal salePrice;
    private String currency;

    // ========== ADDITIONAL ==========
    private List<String> features;
    private List<String> documentIds;
    private List<String> imageUrls;
    private List<String> videoUrls;
    private String virtualTourUrl;
    private Double latitude;
    private Double longitude;
    private Boolean notifyOwnerOnProgress;
    private Boolean notifyOwnerOnCompletion;
    private List<String> tags;
    private String energyCertificate;
    private Boolean isSmartHome;
    private Boolean hasParkingSpace;
    private String parkingNumber;
    private String notes;
}