package com.strux.unit_service.dto;

import com.strux.unit_service.enums.*;
import jakarta.validation.constraints.*;
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

    @NotBlank(message = "Company ID is required")
    private String companyId;

    @NotBlank(message = "Project ID is required")
    private String projectId;

    private String buildingId;

    private String blockName;

    @Min(value = -3, message = "Floor cannot be less than -3")
    @Max(value = 200, message = "Floor cannot exceed 200")
    private Integer floor;

    private String section;

    @NotNull(message = "Unit type is required")
    private UnitType type;

    @NotNull(message = "Gross area is required")
    @DecimalMin(value = "0.1", message = "Gross area must be greater than 0")
    private BigDecimal grossArea;

    @NotNull(message = "Net area is required")
    @DecimalMin(value = "0.1", message = "Net area must be greater than 0")
    private BigDecimal netArea;

    @Min(value = 0, message = "Room count cannot be negative")
    private Integer roomCount;

    @Min(value = 0, message = "Bedroom count cannot be negative")
    private Integer bedroomCount;

    @Min(value = 0, message = "Bathroom count cannot be negative")
    private Integer bathroomCount;

    @Min(value = 0, message = "Balcony count cannot be negative")
    private Integer balconyCount;

    private Direction direction;

    private Boolean hasGarden;
    private BigDecimal gardenArea;
    private Boolean hasTerrace;
    private BigDecimal terraceArea;

    @NotNull(message = "Unit status is required")
    private UnitStatus status;

    private ConstructionPhase currentPhase;

    private LocalDateTime constructionStartDate;
    private LocalDateTime estimatedCompletionDate;

    private SaleStatus saleStatus;

    private BigDecimal listPrice;
    private String currency;

    private List<String> features;
    private List<String> documentIds;
    private List<String> imageUrls;

    private Double latitude;
    private Double longitude;

    private Boolean notifyOwnerOnProgress;
    private Boolean notifyOwnerOnCompletion;

    private List<String> tags;
    private Boolean isSmartHome;
    private Boolean hasParkingSpace;
    private String parkingNumber;

    private String notes;
}
