package com.strux.unit_service.dto;

import com.strux.unit_service.enums.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitUpdateRequest {

    private String unitNumber;
    private String unitName;
    private String description;

    private String buildingId;
    private String blockName;

    @Min(value = -3)
    @Max(value = 200)
    private Integer floor;

    private String section;

    private UnitType type;

    @DecimalMin(value = "0.1")
    private BigDecimal grossArea;

    @DecimalMin(value = "0.1")
    private BigDecimal netArea;

    @Min(value = 0)
    private Integer roomCount;

    @Min(value = 0)
    private Integer bedroomCount;

    @Min(value = 0)
    private Integer bathroomCount;

    @Min(value = 0)
    private Integer balconyCount;

    private Direction direction;

    private Boolean hasGarden;
    private BigDecimal gardenArea;
    private Boolean hasTerrace;
    private BigDecimal terraceArea;

    private UnitStatus status;
    private ConstructionPhase currentPhase;

    private LocalDateTime constructionStartDate;
    private LocalDateTime estimatedCompletionDate;
    private LocalDateTime actualCompletionDate;

    private SaleStatus saleStatus;

    private BigDecimal listPrice;
    private BigDecimal salePrice;
    private String currency;

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
