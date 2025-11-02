package com.strux.unit_service.dto;

import com.strux.unit_service.enums.*;
import com.strux.unit_service.model.InspectionRecord;
import com.strux.unit_service.model.UnitWorkItem;
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

    private String companyId;
    private String projectId;
    private String buildingId;

    private String blockName;
    private Integer floor;
    private String section;

    private UnitType type;
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

    private UnitStatus status;
    private Integer completionPercentage;
    private ConstructionPhase currentPhase;

    private LocalDateTime constructionStartDate;
    private LocalDateTime estimatedCompletionDate;
    private LocalDateTime actualCompletionDate;

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

    private List<InspectionRecord> inspections;
    private Integer qualityScore;
    private Boolean hasDefects;
    private Integer defectCount;

    private List<String> features;
    private List<String> documentIds;
    private List<String> imageUrls;
    private List<String> videoUrls;
    private String virtualTourUrl;

    private Double latitude;
    private Double longitude;

    private List<UnitWorkItemDto> workItems;

    private Boolean notifyOwnerOnProgress;
    private Boolean notifyOwnerOnCompletion;

    private List<String> tags;
    private String energyCertificate;
    private Boolean isSmartHome;
    private Boolean hasParkingSpace;
    private String parkingNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer viewCount;
    private String notes;
}
