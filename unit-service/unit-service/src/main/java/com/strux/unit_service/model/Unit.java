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
    private String footprintJson; // Frontend ucun maps koordinatlari poligon

    @Column(columnDefinition = "TEXT")
    private String description;

    private String companyId;
    private String projectId;

    private String parentUnitId;
    private Boolean hasSubUnits;
    private Integer subUnitsCount;

    // Legacy fields (deprecated, use parentUnitId instead)
    @Deprecated
    private String buildingId;

    private String blockName;
    private Integer floor;
    private String section;

    @Enumerated(EnumType.STRING)
    private UnitType type;  // BUILDING, APARTMENT, VILLA, OFFICE, SHOP, PARKING, STORAGE

    private BigDecimal grossArea;
    private BigDecimal netArea;
    private Integer roomCount;  // Total rooms
    private Integer bedroomCount;
    private Integer bathroomCount;
    private Integer balconyCount;

    @Enumerated(EnumType.STRING)
    private Direction direction;

    private Boolean hasGarden;
    private BigDecimal gardenArea;
    private Boolean hasTerrace;
    private BigDecimal terraceArea;

    // ========== FLOOR PLAN DATA ==========
    @Column(columnDefinition = "TEXT")
    private String floorPlanJson;  // ✅ Fabric.js canvas JSON
    @Column(columnDefinition = "TEXT")
    private String floorPlanImageUrl;  // ✅ PNG/SVG export

    private BigDecimal floorPlanWidth;  // ✅ Actual width in meters
    private BigDecimal floorPlanLength;  // ✅ Actual length in meters
    private BigDecimal ceilingHeight;  // ✅ Ceiling height in meters

    // ========== CONSTRUCTION STATUS ==========
    @Enumerated(EnumType.STRING)
    private UnitStatus status;

    private Integer completionPercentage;

    @Enumerated(EnumType.STRING)
    private ConstructionPhase currentPhase;

    private LocalDateTime constructionStartDate;
    private LocalDateTime estimatedCompletionDate;
    private LocalDateTime actualCompletionDate;

    // ========== SALE INFO ==========
    @Enumerated(EnumType.STRING)
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
    @ElementCollection
    @CollectionTable(name = "unit_inspections", joinColumns = @JoinColumn(name = "unit_id"))
    private List<InspectionRecord> inspections;

    private Integer qualityScore;
    private Boolean hasDefects;
    private Integer defectCount;

    // ========== FEATURES & MEDIA ==========
    @ElementCollection
    @CollectionTable(name = "unit_features", joinColumns = @JoinColumn(name = "unit_id"))
    @Column(name = "feature")
    private List<String> features;

    @ElementCollection
    @CollectionTable(name = "unit_documents", joinColumns = @JoinColumn(name = "unit_id"))
    @Column(name = "document_id")
    private List<String> documentIds;

    @ElementCollection
    @CollectionTable(name = "unit_images", joinColumns = @JoinColumn(name = "unit_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;

    @ElementCollection
    @CollectionTable(name = "unit_videos", joinColumns = @JoinColumn(name = "unit_id"))
    @Column(name = "video_url")
    private List<String> videoUrls;

    private String virtualTourUrl;

    // ========== LOCATION ==========
    private Double latitude;
    private Double longitude;

    // ========== WORK ITEMS ==========
    @OneToMany(mappedBy = "unitId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UnitWorkItem> workItems;

    // ========== NOTIFICATIONS ==========
    private Boolean notifyOwnerOnProgress;
    private Boolean notifyOwnerOnCompletion;

    // ========== ADDITIONAL INFO ==========
    @ElementCollection
    @CollectionTable(name = "unit_tags", joinColumns = @JoinColumn(name = "unit_id"))
    @Column(name = "tag")
    private List<String> tags;

    private String energyCertificate;
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

    @Column(columnDefinition = "TEXT")
    private String notes;
}