package com.strux.unit_service.model;

import com.strux.unit_service.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "units")
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String unitNumber;
    private String unitName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String companyId;
    private String projectId;
    private String buildingId;

    private String blockName;
    private Integer floor;
    private String section;


    @Enumerated(EnumType.STRING)
    private UnitType type;  // APARTMENT, VILLA, OFFICE, SHOP, PARKING, STORAGE

    private BigDecimal grossArea;
    private BigDecimal netArea;
    private Integer roomCount;
    private Integer bedroomCount;
    private Integer bathroomCount;
    private Integer balconyCount;

    @Enumerated(EnumType.STRING)
    private Direction direction;  // NORTH, SOUTH, EAST, WEST, NORTH_EAST, etc.

    private Boolean hasGarden;
    private BigDecimal gardenArea;
    private Boolean hasTerrace;
    private BigDecimal terraceArea;

    @Enumerated(EnumType.STRING)
    private UnitStatus status;  // PLANNED, IN_CONSTRUCTION, COMPLETED, SOLD, DELIVERED

    private Integer completionPercentage;  // 0-100

    @Enumerated(EnumType.STRING)
    private ConstructionPhase currentPhase;  // FOUNDATION, STRUCTURE, ROUGH_CONSTRUCTION, FINISHING, etc.

    private LocalDateTime constructionStartDate;
    private LocalDateTime estimatedCompletionDate;
    private LocalDateTime actualCompletionDate;

    @Enumerated(EnumType.STRING)
    private SaleStatus saleStatus;  // AVAILABLE, RESERVED, SOLD, NOT_FOR_SALE

    private String ownerId;  // Sahibkar ID
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;

    private LocalDateTime reservationDate;
    private LocalDateTime saleDate;
    private LocalDateTime deliveryDate;

    private BigDecimal listPrice;
    private BigDecimal salePrice;
    private String currency;  // AZN, USD, EUR
    private BigDecimal pricePerSquareMeter;

    private BigDecimal totalPaid;
    private BigDecimal remainingPayment;
    private Integer paymentPercentage;  // Ödəniş faizi

    @ElementCollection
    @CollectionTable(name = "unit_inspections", joinColumns = @JoinColumn(name = "unit_id"))
    private List<InspectionRecord> inspections;

    private Integer qualityScore;  // 1-100
    private Boolean hasDefects;
    private Integer defectCount;

    @ElementCollection
    @CollectionTable(name = "unit_features", joinColumns = @JoinColumn(name = "unit_id"))
    @Column(name = "feature")
    private List<String> features;  // Smart Home, Central Heating, Security System, etc.

    @ElementCollection
    @CollectionTable(name = "unit_documents", joinColumns = @JoinColumn(name = "unit_id"))
    @Column(name = "document_id")
    private List<String> documentIds;  // Floor plans, contracts, certificates

    @ElementCollection
    @CollectionTable(name = "unit_images", joinColumns = @JoinColumn(name = "unit_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;  // Photos

    @ElementCollection
    @CollectionTable(name = "unit_videos", joinColumns = @JoinColumn(name = "unit_id"))
    @Column(name = "video_url")
    private List<String> videoUrls;

    private String virtualTourUrl;

    private Double latitude;
    private Double longitude;

    @OneToMany(mappedBy = "unitId", cascade = CascadeType.ALL)
    private List<UnitWorkItem> workItems;


    private Boolean notifyOwnerOnProgress;
    private Boolean notifyOwnerOnCompletion;


    @ElementCollection
    @CollectionTable(name = "unit_tags", joinColumns = @JoinColumn(name = "unit_id"))
    @Column(name = "tag")
    private List<String> tags;

    private String energyCertificate;  // Energy efficiency rating
    private Boolean isSmartHome;
    private Boolean hasParkingSpace;
    private String parkingNumber;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private Integer viewCount;

    private String notes;
}
