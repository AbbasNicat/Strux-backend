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
public class UnitDto {

    private String id;
    private String unitNumber;
    private String unitName;
    private String description;
    private String footprintJson;


    private String companyId;
    private String projectId;

    // ========== HIERARCHICAL STRUCTURE ==========
    private String parentUnitId;
    private Boolean hasSubUnits;
    private Integer subUnitsCount;

    @Deprecated
    private String buildingId;

    private String blockName;
    private Integer floor;
    private String section;

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
    private String floorPlanJson;
    private String floorPlanImageUrl;
    private BigDecimal floorPlanWidth;
    private BigDecimal floorPlanLength;
    private BigDecimal ceilingHeight;

    // ========== CONSTRUCTION STATUS ==========
    private UnitStatus status;
    private Integer completionPercentage;
    private ConstructionPhase currentPhase;
    private LocalDateTime constructionStartDate;
    private LocalDateTime estimatedCompletionDate;
    private LocalDateTime actualCompletionDate;

    // ========== SALE INFO ==========
    private SaleStatus saleStatus;
    private String ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;
    private LocalDateTime reservationDate;
    private LocalDateTime saleDate;
    private LocalDateTime deliveryDate;
    private BigDecimal listPrice;
    private BigDecimal salePrice;
    private String currency;
    private BigDecimal pricePerSquareMeter;
    private BigDecimal totalPaid;
    private BigDecimal remainingPayment;
    private Integer paymentPercentage;

    // ========== QUALITY & INSPECTIONS ==========
    private Integer qualityScore;
    private Boolean hasDefects;
    private Integer defectCount;

    // ========== FEATURES & MEDIA ==========
    private List<String> features;
    private List<String> documentIds;
    private List<String> imageUrls;
    private List<String> videoUrls;
    private String virtualTourUrl;

    // ========== LOCATION ==========
    private Double latitude;
    private Double longitude;

    // ========== WORK ITEMS ==========
    private List<UnitWorkItemDto> workItems;

    // ========== NOTIFICATIONS ==========
    private Boolean notifyOwnerOnProgress;
    private Boolean notifyOwnerOnCompletion;

    // ========== ADDITIONAL INFO ==========
    private List<String> tags;
    private String energyCertificate;
    private Boolean isSmartHome;
    private Boolean hasParkingSpace;
    private String parkingNumber;
    private Integer viewCount;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}