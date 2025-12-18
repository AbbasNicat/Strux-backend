package com.strux.unit_service.service;

import com.strux.unit_service.dto.*;
import com.strux.unit_service.enums.*;
import com.strux.unit_service.model.Unit;
import com.strux.unit_service.model.UnitWorkItem;
import com.strux.unit_service.repository.UnitRepository;
import com.strux.unit_service.repository.UnitWorkItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnitService {

    private final UnitRepository unitRepository;
    private final UnitWorkItemRepository workItemRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WebClient.Builder webClientBuilder;

    @Transactional(readOnly = true)
    public List<UserResponse> getUnitWorkers(String unitId) {
        log.info("Fetching workers for unit: {}", unitId);

        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unitId));

        try {
            String token = getAuthToken();

            List<UserResponse> workers = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:9091/api/workers/unit/{unitId}", unitId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToFlux(UserResponse.class)
                    .collectList()
                    .block();

            log.info("Found {} workers for unit {}",
                    workers != null ? workers.size() : 0, unitId);

            return workers != null ? workers : List.of();

        } catch (Exception e) {
            log.error("Failed to fetch workers for unit {}: {}", unitId, e.getMessage());
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public Long getUnitCount(String projectId) {
        return unitRepository.countActualUnitsByProject(projectId);
    }


    private String getAuthToken() {
        try {
            org.springframework.security.core.Authentication authentication =
                    org.springframework.security.core.context.SecurityContextHolder
                            .getContext()
                            .getAuthentication();

            if (authentication != null &&
                    authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
                return jwt.getTokenValue();
            }

            log.warn("No JWT token found in SecurityContext");
            return null;
        } catch (Exception e) {
            log.error("Error getting auth token: {}", e.getMessage());
            return null;
        }
    }
    @Transactional(readOnly = true)
    public List<UnitMapInfo> getProjectUnitsForMap(String projectId) {
        log.info("📍 Fetching map info for project: {}", projectId);

        // ✅ Yalnız parent unit'leri getir (building level, sub-unit'ler yox)
        List<Unit> units = unitRepository.findActualUnitsByProjectAndParentIsNull(projectId);

        return units.stream()
                .map(unit -> UnitMapInfo.builder()
                        .id(unit.getId())
                        .unitNumber(unit.getUnitNumber())
                        .unitName(unit.getUnitName())
                        .type(unit.getType())
                        .latitude(unit.getLatitude())
                        .longitude(unit.getLongitude())
                        .completionPercentage(unit.getCompletionPercentage())
                        .subUnitsCount(unit.getSubUnitsCount())
                        .hasSubUnits(unit.getHasSubUnits())
                        .build())
                .collect(Collectors.toList());
    }
    @Transactional
    public UnitDto createUnit(UnitCreateRequest request) {
        log.info("Creating unit with number: {}", request.getUnitNumber());

        if (unitRepository.existsByProjectIdAndUnitNumberAndDeletedAtIsNull(
                request.getProjectId(),
                request.getUnitNumber())) {
            throw new RuntimeException("Unit number already exists in this project");
        }

        Unit unit = Unit.builder()
                .unitNumber(request.getUnitNumber())
                .unitName(request.getUnitName())
                .description(request.getDescription())
                .companyId(request.getCompanyId())
                .projectId(request.getProjectId())
                .footprintJson(request.getFootprintJson())

                // ✅ HIERARCHICAL STRUCTURE
                .parentUnitId(request.getParentUnitId())
                .hasSubUnits(request.getHasSubUnits() != null ? request.getHasSubUnits() : false)
                .subUnitsCount(0)

                .buildingId(request.getBuildingId())
                .blockName(request.getBlockName())
                .floor(request.getFloor())
                .section(request.getSection())
                .type(request.getType())
                .grossArea(request.getGrossArea())
                .netArea(request.getNetArea())
                .roomCount(request.getRoomCount())
                .bedroomCount(request.getBedroomCount())
                .bathroomCount(request.getBathroomCount())
                .balconyCount(request.getBalconyCount())
                .direction(request.getDirection())
                .hasGarden(request.getHasGarden() != null ? request.getHasGarden() : false)
                .gardenArea(request.getGardenArea())
                .hasTerrace(request.getHasTerrace() != null ? request.getHasTerrace() : false)
                .terraceArea(request.getTerraceArea())

                // ✅ FLOOR PLAN DATA
                .floorPlanJson(request.getFloorPlanJson())
                .floorPlanImageUrl(request.getFloorPlanImageUrl())
                .floorPlanWidth(request.getFloorPlanWidth())
                .floorPlanLength(request.getFloorPlanLength())
                .ceilingHeight(request.getCeilingHeight())

                .status(request.getStatus())
                .currentPhase(request.getCurrentPhase())
                .constructionStartDate(request.getConstructionStartDate())
                .estimatedCompletionDate(request.getEstimatedCompletionDate())
                .actualCompletionDate(request.getActualCompletionDate())
                .saleStatus(request.getSaleStatus() != null ? request.getSaleStatus() : SaleStatus.NOT_FOR_SALE)
                .listPrice(request.getListPrice())
                .salePrice(request.getSalePrice())
                .currency(request.getCurrency() != null ? request.getCurrency() : "AZN")
                .features(request.getFeatures())
                .documentIds(request.getDocumentIds())
                .imageUrls(request.getImageUrls())
                .videoUrls(request.getVideoUrls())
                .virtualTourUrl(request.getVirtualTourUrl())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .notifyOwnerOnProgress(request.getNotifyOwnerOnProgress() != null ? request.getNotifyOwnerOnProgress() : false)
                .notifyOwnerOnCompletion(request.getNotifyOwnerOnCompletion() != null ? request.getNotifyOwnerOnCompletion() : false)
                .tags(request.getTags())
                .energyCertificate(request.getEnergyCertificate())
                .isSmartHome(request.getIsSmartHome() != null ? request.getIsSmartHome() : false)
                .hasParkingSpace(request.getHasParkingSpace() != null ? request.getHasParkingSpace() : false)
                .parkingNumber(request.getParkingNumber())
                .notes(request.getNotes())
                .completionPercentage(0)
                .viewCount(0)
                .build();

        if (request.getListPrice() != null && request.getNetArea() != null && request.getNetArea().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pricePerSqm = request.getListPrice()
                    .divide(request.getNetArea(), 2, RoundingMode.HALF_UP);
            unit.setPricePerSquareMeter(pricePerSqm);
        }

        Unit savedUnit = unitRepository.save(unit);

        // ✅ If has parent, increment parent's subUnitsCount
        if (savedUnit.getParentUnitId() != null) {
            unitRepository.findById(savedUnit.getParentUnitId()).ifPresent(parent -> {
                parent.setSubUnitsCount((parent.getSubUnitsCount() != null ? parent.getSubUnitsCount() : 0) + 1);
                unitRepository.save(parent);
            });
        }

        log.info("Unit created successfully with ID: {}", savedUnit.getId());
        return mapToDto(savedUnit);
    }

    private void recalculateParentCompletion(String parentUnitId) {
        List<Unit> subUnits = unitRepository.findActualSubUnits(parentUnitId);

        if (subUnits.isEmpty()) {
            return;
        }

        // ✅ Sub-unit'lərin orta progress'i
        double avgProgress = subUnits.stream()
                .mapToInt(u -> u.getCompletionPercentage() != null ? u.getCompletionPercentage() : 0)
                .average()
                .orElse(0.0);

        Unit parent = unitRepository.findById(parentUnitId).orElse(null);
        if (parent != null) {
            parent.setCompletionPercentage((int) Math.round(avgProgress));
            unitRepository.save(parent);

            log.info("✅ Parent unit {} progress updated to {}%", parentUnitId, (int) Math.round(avgProgress));
        }
    }

    private UnitDto mapToDto(Unit unit) {
        return UnitDto.builder()
                .id(unit.getId())
                .unitNumber(unit.getUnitNumber())
                .unitName(unit.getUnitName())
                .description(unit.getDescription())
                .companyId(unit.getCompanyId())
                .projectId(unit.getProjectId())
                .footprintJson(unit.getFootprintJson())
                .parentUnitId(unit.getParentUnitId())
                .hasSubUnits(unit.getHasSubUnits())
                .subUnitsCount(unit.getSubUnitsCount())
                .buildingId(unit.getBuildingId())
                .blockName(unit.getBlockName())
                .floor(unit.getFloor())
                .section(unit.getSection())
                .type(unit.getType())
                .grossArea(unit.getGrossArea())
                .netArea(unit.getNetArea())
                .roomCount(unit.getRoomCount())
                .bedroomCount(unit.getBedroomCount())
                .bathroomCount(unit.getBathroomCount())
                .balconyCount(unit.getBalconyCount())
                .direction(unit.getDirection())
                .hasGarden(unit.getHasGarden())
                .gardenArea(unit.getGardenArea())
                .hasTerrace(unit.getHasTerrace())
                .terraceArea(unit.getTerraceArea())
                .floorPlanJson(unit.getFloorPlanJson())
                .floorPlanImageUrl(unit.getFloorPlanImageUrl())
                .floorPlanWidth(unit.getFloorPlanWidth())
                .floorPlanLength(unit.getFloorPlanLength())
                .ceilingHeight(unit.getCeilingHeight())
                .status(unit.getStatus())
                .completionPercentage(unit.getCompletionPercentage())
                .currentPhase(unit.getCurrentPhase())
                .constructionStartDate(unit.getConstructionStartDate())
                .estimatedCompletionDate(unit.getEstimatedCompletionDate())
                .actualCompletionDate(unit.getActualCompletionDate())
                .saleStatus(unit.getSaleStatus())
                .ownerId(unit.getOwnerId())
                .ownerName(unit.getOwnerName())
                .ownerEmail(unit.getOwnerEmail())
                .ownerPhone(unit.getOwnerPhone())
                .reservationDate(unit.getReservationDate())
                .saleDate(unit.getSaleDate())
                .deliveryDate(unit.getDeliveryDate())
                .listPrice(unit.getListPrice())
                .salePrice(unit.getSalePrice())
                .currency(unit.getCurrency())
                .pricePerSquareMeter(unit.getPricePerSquareMeter())
                .totalPaid(unit.getTotalPaid())
                .remainingPayment(unit.getRemainingPayment())
                .paymentPercentage(unit.getPaymentPercentage())
                .qualityScore(unit.getQualityScore())
                .hasDefects(unit.getHasDefects())
                .defectCount(unit.getDefectCount())
                .features(unit.getFeatures())
                .documentIds(unit.getDocumentIds())
                .imageUrls(unit.getImageUrls())
                .videoUrls(unit.getVideoUrls())
                .virtualTourUrl(unit.getVirtualTourUrl())
                .latitude(unit.getLatitude())
                .longitude(unit.getLongitude())
                .notifyOwnerOnProgress(unit.getNotifyOwnerOnProgress())
                .notifyOwnerOnCompletion(unit.getNotifyOwnerOnCompletion())
                .tags(unit.getTags())
                .energyCertificate(unit.getEnergyCertificate())
                .isSmartHome(unit.getIsSmartHome())
                .hasParkingSpace(unit.getHasParkingSpace())
                .parkingNumber(unit.getParkingNumber())
                .viewCount(unit.getViewCount())
                .notes(unit.getNotes())
                .createdAt(unit.getCreatedAt())
                .updatedAt(unit.getUpdatedAt())
                .build();
    }

    public UnitDto getUnit(String unitId) {
        Unit unit = unitRepository.findById(unitId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Unit not found"));

        unit.setViewCount(unit.getViewCount() != null ? unit.getViewCount() + 1 : 1);
        unitRepository.save(unit);

        return toDto(unit);
    }

    @Transactional(readOnly = true)
    public List<UnitDto> getUnitsByProject(String projectId) {
        // ✅ Sadece gerçek unit'leri getir (floor schema'lar hariç)
        return unitRepository.findActualUnitsByProject(projectId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UnitDto> getUnitsByBuilding(String buildingId) {
        // ✅ Sadece gerçek apartment/unit'leri getir (floor schema'lar hariç)
        return unitRepository.findActualSubUnits(buildingId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UnitDto> getUnitsByBlock(String projectId, String blockName) {
        return unitRepository.findActualUnitsByProjectAndBlock(projectId, blockName)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UnitDto> getUnitsByFloor(String projectId, Integer floor) {
        return unitRepository.findActualUnitsByProjectAndFloor(projectId, floor)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<UnitDto> getUnitsByStatus(String projectId, UnitStatus status) {
        return unitRepository.findByProjectIdAndStatusAndDeletedAtIsNull(projectId, status)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<UnitDto> getUnitsBySaleStatus(String projectId, SaleStatus saleStatus) {
        return unitRepository.findByProjectIdAndSaleStatusAndDeletedAtIsNull(projectId, saleStatus)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<UnitDto> getUnitsByType(String projectId, UnitType type) {
        return unitRepository.findByProjectIdAndTypeAndDeletedAtIsNull(projectId, type)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<UnitDto> getUnitsByOwner(String ownerId) {
        return unitRepository.findByOwnerIdAndDeletedAtIsNull(ownerId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<UnitDto> getAvailableUnits(String projectId) {
        return unitRepository.findByProjectIdAndSaleStatusAndDeletedAtIsNull(projectId, SaleStatus.AVAILABLE)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<UnitDto> getOverdueUnits(String projectId) {
        List<UnitStatus> excludedStatuses = Arrays.asList(
                UnitStatus.COMPLETED,
                UnitStatus.DELIVERED
        );

        return unitRepository.findByProjectIdAndEstimatedCompletionDateBeforeAndStatusNotInAndDeletedAtIsNull(
                        projectId,
                        LocalDateTime.now(),
                        excludedStatuses
                )
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<UnitDto> searchUnits(UnitSearchRequest request) {
        List<Unit> units = unitRepository.findByProjectIdAndDeletedAtIsNull(request.getProjectId());

        return units.stream()
                .filter(unit -> matchesSearchCriteria(unit, request))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UnitDto updateUnit(String unitId, UnitUpdateRequest request) {
        Unit unit = unitRepository.findById(unitId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Unit not found"));

        // ✅ Basic Info Updates
        if (request.getUnitNumber() != null) unit.setUnitNumber(request.getUnitNumber());
        if (request.getUnitName() != null) unit.setUnitName(request.getUnitName());
        if (request.getDescription() != null) unit.setDescription(request.getDescription());
        if (request.getBuildingId() != null) unit.setBuildingId(request.getBuildingId());
        if (request.getBlockName() != null) unit.setBlockName(request.getBlockName());
        if (request.getFloor() != null) unit.setFloor(request.getFloor());
        if (request.getFootprintJson() != null) unit.setFootprintJson(request.getFootprintJson());
        if (request.getSection() != null) unit.setSection(request.getSection());
        if (request.getType() != null) unit.setType(request.getType());
        if (request.getGrossArea() != null) unit.setGrossArea(request.getGrossArea());

        // ✅ Area & Price Calculation
        if (request.getNetArea() != null) {
            unit.setNetArea(request.getNetArea());
            if (unit.getListPrice() != null && request.getNetArea().compareTo(BigDecimal.ZERO) > 0) {
                unit.setPricePerSquareMeter(unit.getListPrice().divide(request.getNetArea(), 2, RoundingMode.HALF_UP));
            }
        }

        // ✅ Room Details
        if (request.getRoomCount() != null) unit.setRoomCount(request.getRoomCount());
        if (request.getBedroomCount() != null) unit.setBedroomCount(request.getBedroomCount());
        if (request.getBathroomCount() != null) unit.setBathroomCount(request.getBathroomCount());
        if (request.getBalconyCount() != null) unit.setBalconyCount(request.getBalconyCount());
        if (request.getDirection() != null) unit.setDirection(request.getDirection());
        if (request.getHasGarden() != null) unit.setHasGarden(request.getHasGarden());
        if (request.getGardenArea() != null) unit.setGardenArea(request.getGardenArea());
        if (request.getHasTerrace() != null) unit.setHasTerrace(request.getHasTerrace());
        if (request.getTerraceArea() != null) unit.setTerraceArea(request.getTerraceArea());

        // ✅ Floor Plan Updates
        if (request.getFloorPlanJson() != null) unit.setFloorPlanJson(request.getFloorPlanJson());
        if (request.getFloorPlanImageUrl() != null) unit.setFloorPlanImageUrl(request.getFloorPlanImageUrl());
        if (request.getFloorPlanWidth() != null) unit.setFloorPlanWidth(request.getFloorPlanWidth());
        if (request.getFloorPlanLength() != null) unit.setFloorPlanLength(request.getFloorPlanLength());
        if (request.getCeilingHeight() != null) unit.setCeilingHeight(request.getCeilingHeight());

        // ✅ CRITICAL FIX: Completion Percentage Update
        boolean progressUpdated = false;
        if (request.getCompletionPercentage() != null) {
            unit.setCompletionPercentage(request.getCompletionPercentage());
            progressUpdated = true;

            log.info("✅ Unit {} completion updated to {}%", unitId, request.getCompletionPercentage());
        }

        // ✅ Status Update (with auto-completion)
        if (request.getStatus() != null) {
            UnitStatus oldStatus = unit.getStatus();
            unit.setStatus(request.getStatus());

            if (request.getStatus() == UnitStatus.COMPLETED && unit.getActualCompletionDate() == null) {
                unit.setActualCompletionDate(LocalDateTime.now());
                unit.setCompletionPercentage(100);
                progressUpdated = true;
            }

            publishUnitStatusChangedEvent(unit, oldStatus);
        }

        // ✅ Current Phase
        if (request.getCurrentPhase() != null) unit.setCurrentPhase(request.getCurrentPhase());

        // ✅ Construction Dates
        if (request.getConstructionStartDate() != null) unit.setConstructionStartDate(request.getConstructionStartDate());
        if (request.getEstimatedCompletionDate() != null) unit.setEstimatedCompletionDate(request.getEstimatedCompletionDate());
        if (request.getActualCompletionDate() != null) unit.setActualCompletionDate(request.getActualCompletionDate());

        // ✅ Sale Info
        if (request.getSaleStatus() != null) unit.setSaleStatus(request.getSaleStatus());

        if (request.getListPrice() != null) {
            unit.setListPrice(request.getListPrice());
            if (unit.getNetArea() != null && unit.getNetArea().compareTo(BigDecimal.ZERO) > 0) {
                unit.setPricePerSquareMeter(request.getListPrice().divide(unit.getNetArea(), 2, RoundingMode.HALF_UP));
            }
        }

        if (request.getSalePrice() != null) unit.setSalePrice(request.getSalePrice());
        if (request.getCurrency() != null) unit.setCurrency(request.getCurrency());

        // ✅ Features & Media
        if (request.getFeatures() != null) unit.setFeatures(request.getFeatures());
        if (request.getDocumentIds() != null) unit.setDocumentIds(request.getDocumentIds());
        if (request.getImageUrls() != null) unit.setImageUrls(request.getImageUrls());
        if (request.getVideoUrls() != null) unit.setVideoUrls(request.getVideoUrls());
        if (request.getVirtualTourUrl() != null) unit.setVirtualTourUrl(request.getVirtualTourUrl());

        // ✅ Location
        if (request.getLatitude() != null) unit.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) unit.setLongitude(request.getLongitude());

        // ✅ Notifications
        if (request.getNotifyOwnerOnProgress() != null) unit.setNotifyOwnerOnProgress(request.getNotifyOwnerOnProgress());
        if (request.getNotifyOwnerOnCompletion() != null) unit.setNotifyOwnerOnCompletion(request.getNotifyOwnerOnCompletion());

        // ✅ Additional Info
        if (request.getTags() != null) unit.setTags(request.getTags());
        if (request.getEnergyCertificate() != null) unit.setEnergyCertificate(request.getEnergyCertificate());
        if (request.getIsSmartHome() != null) unit.setIsSmartHome(request.getIsSmartHome());
        if (request.getHasParkingSpace() != null) unit.setHasParkingSpace(request.getHasParkingSpace());
        if (request.getParkingNumber() != null) unit.setParkingNumber(request.getParkingNumber());
        if (request.getNotes() != null) unit.setNotes(request.getNotes());

        // ✅ Save the unit
        unit = unitRepository.save(unit);

        log.info("✅ Unit {} updated successfully", unitId);

        // ✅ CRITICAL FIX: If this is a sub-unit and progress was updated, recalculate parent
        if (progressUpdated && unit.getParentUnitId() != null) {
            log.info("🔄 Recalculating parent unit {} progress because sub-unit {} was updated",
                    unit.getParentUnitId(), unitId);
            recalculateParentCompletion(unit.getParentUnitId());
        }

        // ✅ Publish events
        publishUnitUpdatedEvent(unit);

        return toDto(unit);
    }


    @Transactional
    public UnitDto updateProgress(String unitId, UnitProgressUpdateRequest request) {
        Unit unit = unitRepository.findById(unitId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Unit not found"));

        Integer oldPercentage = unit.getCompletionPercentage();
        unit.setCompletionPercentage(request.getCompletionPercentage());

        if (request.getCurrentPhase() != null) {
            unit.setCurrentPhase(request.getCurrentPhase());
        }

        if (request.getCompletionPercentage() == 100 && unit.getStatus() != UnitStatus.COMPLETED) {
            unit.setStatus(UnitStatus.COMPLETED);
            unit.setActualCompletionDate(LocalDateTime.now());
        }

        unit = unitRepository.save(unit);

        publishUnitProgressUpdatedEvent(unit, oldPercentage);

        if (unit.getNotifyOwnerOnProgress() && unit.getOwnerId() != null) {
            publishOwnerNotificationEvent(unit, "progress");
        }

        if (unit.getCompletionPercentage() == 100 && unit.getNotifyOwnerOnCompletion() && unit.getOwnerId() != null) {
            publishOwnerNotificationEvent(unit, "completion");
        }

        return toDto(unit);
    }

    @Transactional
    public UnitDto reserveUnit(String unitId, UnitReservationRequest request) {
        Unit unit = unitRepository.findById(unitId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Unit not found"));

        if (unit.getSaleStatus() != SaleStatus.AVAILABLE) {
            throw new RuntimeException("Unit is not available for reservation");
        }

        unit.setSaleStatus(SaleStatus.RESERVED);
        unit.setOwnerId(request.getOwnerId());
        unit.setOwnerName(request.getOwnerName());
        unit.setOwnerEmail(request.getOwnerEmail());
        unit.setOwnerPhone(request.getOwnerPhone());
        unit.setReservationDate(request.getReservationDate() != null ? request.getReservationDate() : LocalDateTime.now());

        unit = unitRepository.save(unit);
        publishUnitReservedEvent(unit);
        return toDto(unit);
    }

    @Transactional
    public UnitDto sellUnit(String unitId, UnitSaleRequest request) {
        Unit unit = unitRepository.findById(unitId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Unit not found"));

        if (unit.getSaleStatus() == SaleStatus.SOLD) {
            throw new RuntimeException("Unit is already sold");
        }

        unit.setSaleStatus(SaleStatus.SOLD);
        unit.setOwnerId(request.getOwnerId());
        unit.setOwnerName(request.getOwnerName());
        unit.setOwnerEmail(request.getOwnerEmail());
        unit.setOwnerPhone(request.getOwnerPhone());
        unit.setSalePrice(request.getSalePrice());
        unit.setSaleDate(request.getSaleDate() != null ? request.getSaleDate() : LocalDateTime.now());
        unit.setRemainingPayment(request.getSalePrice());
        unit.setPaymentPercentage(0);

        unit = unitRepository.save(unit);
        publishUnitSoldEvent(unit);
        return toDto(unit);
    }

    @Transactional
    public UnitDto cancelReservation(String unitId) {
        Unit unit = unitRepository.findById(unitId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Unit not found"));

        if (unit.getSaleStatus() != SaleStatus.RESERVED) {
            throw new RuntimeException("Unit is not reserved");
        }

        unit.setSaleStatus(SaleStatus.AVAILABLE);
        unit.setOwnerId(null);
        unit.setOwnerName(null);
        unit.setOwnerEmail(null);
        unit.setOwnerPhone(null);
        unit.setReservationDate(null);

        unit = unitRepository.save(unit);
        publishUnitReservationCancelledEvent(unit);
        return toDto(unit);
    }

    @Transactional
    public void deleteUnit(String unitId, boolean hardDelete) {
        Unit unit = unitRepository.findById(unitId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Unit not found"));

        if (hardDelete) {
            workItemRepository.deleteByUnitId(unitId);
            unitRepository.delete(unit);
        } else {
            unit.setDeletedAt(LocalDateTime.now());
            unitRepository.save(unit);
        }

        publishUnitDeletedEvent(unit, hardDelete);
    }

    @Transactional
    public UnitWorkItemDto addWorkItem(String unitId, WorkItemCreateRequest request) {
        Unit unit = unitRepository.findById(unitId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Unit not found"));

        UnitWorkItem workItem = UnitWorkItem.builder()
                .unitId(unitId)
                .workName(request.getWorkName())
                .description(request.getDescription())
                .status(request.getStatus())
                .completionPercentage(0)
                .weightPercentage(request.getWeightPercentage())
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .assignedContractorId(request.getAssignedContractorId())
                .assignedWorkerId(request.getAssignedWorkerId())
                .taskId(request.getTaskId())
                .build();

        workItem = workItemRepository.save(workItem);
        publishWorkItemCreatedEvent(workItem, unit);
        return toWorkItemDto(workItem);
    }

    @Transactional
    public UnitWorkItemDto updateWorkItem(String workItemId, WorkItemUpdateRequest request) {
        UnitWorkItem workItem = workItemRepository.findById(workItemId)
                .orElseThrow(() -> new RuntimeException("Work item not found"));

        Integer oldPercentage = workItem.getCompletionPercentage();

        if (request.getWorkName() != null) workItem.setWorkName(request.getWorkName());
        if (request.getDescription() != null) workItem.setDescription(request.getDescription());

        if (request.getStatus() != null) {
            workItem.setStatus(request.getStatus());
            if (request.getStatus() == WorkItemStatus.COMPLETED && workItem.getCompletedAt() == null) {
                workItem.setCompletedAt(LocalDateTime.now());
                workItem.setCompletionPercentage(100);
            }
        }

        if (request.getCompletionPercentage() != null) workItem.setCompletionPercentage(request.getCompletionPercentage());
        if (request.getWeightPercentage() != null) workItem.setWeightPercentage(request.getWeightPercentage());
        if (request.getStartDate() != null) workItem.setStartDate(request.getStartDate());
        if (request.getDueDate() != null) workItem.setDueDate(request.getDueDate());
        if (request.getCompletedAt() != null) workItem.setCompletedAt(request.getCompletedAt());
        if (request.getAssignedContractorId() != null) workItem.setAssignedContractorId(request.getAssignedContractorId());
        if (request.getAssignedWorkerId() != null) workItem.setAssignedWorkerId(request.getAssignedWorkerId());
        if (request.getTaskId() != null) workItem.setTaskId(request.getTaskId());

        workItem = workItemRepository.save(workItem);

        recalculateUnitCompletion(workItem.getUnitId());
        publishWorkItemUpdatedEvent(workItem, oldPercentage);

        return toWorkItemDto(workItem);
    }

    @Transactional
    public void deleteWorkItem(String workItemId) {
        UnitWorkItem workItem = workItemRepository.findById(workItemId)
                .orElseThrow(() -> new RuntimeException("Work item not found"));

        String unitId = workItem.getUnitId();
        workItemRepository.delete(workItem);
        recalculateUnitCompletion(unitId);
    }

    public List<UnitWorkItemDto> getWorkItems(String unitId) {
        return workItemRepository.findByUnitId(unitId)
                .stream()
                .map(this::toWorkItemDto)
                .collect(Collectors.toList());
    }

    private void recalculateUnitCompletion(String unitId) {
        List<UnitWorkItem> workItems = workItemRepository.findByUnitId(unitId);
        if (workItems.isEmpty()) return;

        int totalWeight = workItems.stream().mapToInt(UnitWorkItem::getWeightPercentage).sum();
        if (totalWeight == 0) return;

        double weightedCompletion = workItems.stream()
                .mapToDouble(item -> (item.getCompletionPercentage() * item.getWeightPercentage()) / 100.0)
                .sum();

        int unitCompletion = (int) ((weightedCompletion / totalWeight) * 100);

        Unit unit = unitRepository.findById(unitId).orElse(null);
        if (unit != null) {
            unit.setCompletionPercentage(unitCompletion);
            unitRepository.save(unit);
        }
    }

    public UnitStatsResponse getUnitStats(String projectId) {
        Long totalUnits = unitRepository.countActualUnitsByProject(projectId);
        Long plannedUnits = unitRepository.countByProjectIdAndStatusAndDeletedAtIsNull(projectId, UnitStatus.PLANNED);
        Long inConstructionUnits = unitRepository.countByProjectIdAndStatusAndDeletedAtIsNull(projectId, UnitStatus.IN_CONSTRUCTION);
        Long completedUnits = unitRepository.countByProjectIdAndStatusAndDeletedAtIsNull(projectId, UnitStatus.COMPLETED);
        Long deliveredUnits = unitRepository.countByProjectIdAndStatusAndDeletedAtIsNull(projectId, UnitStatus.DELIVERED);
        Long availableUnits = unitRepository.countByProjectIdAndSaleStatusAndDeletedAtIsNull(projectId, SaleStatus.AVAILABLE);
        Long reservedUnits = unitRepository.countByProjectIdAndSaleStatusAndDeletedAtIsNull(projectId, SaleStatus.RESERVED);
        Long soldUnits = unitRepository.countByProjectIdAndSaleStatusAndDeletedAtIsNull(projectId, SaleStatus.SOLD);

        Map<String, Long> unitsByType = convertToMap(unitRepository.countByTypeGrouped(projectId));
        Map<String, Long> unitsByStatus = convertToMap(unitRepository.countByStatusGrouped(projectId));
        Map<String, Long> unitsBySaleStatus = convertToMap(unitRepository.countBySaleStatusGrouped(projectId));
        Map<String, Long> unitsByPhase = convertToMap(unitRepository.countByPhaseGrouped(projectId));

        Double averageCompletion = unitRepository.getAverageCompletionPercentage(projectId);
        Double totalGrossArea = unitRepository.getTotalGrossArea(projectId);
        Double totalNetArea = unitRepository.getTotalNetArea(projectId);

        BigDecimal totalSalesValue = unitRepository.getTotalSalesValue(projectId);
        BigDecimal totalCollectedPayments = unitRepository.getTotalCollectedPayments(projectId);
        BigDecimal totalRemainingPayments = unitRepository.getTotalRemainingPayments(projectId);
        Integer averageQualityScore = unitRepository.getAverageQualityScore(projectId);

        return UnitStatsResponse.builder()
                .totalUnits(totalUnits)
                .plannedUnits(plannedUnits)
                .inConstructionUnits(inConstructionUnits)
                .completedUnits(completedUnits)
                .deliveredUnits(deliveredUnits)
                .availableUnits(availableUnits)
                .reservedUnits(reservedUnits)
                .soldUnits(soldUnits)
                .unitsByType(unitsByType)
                .unitsByStatus(unitsByStatus)
                .unitsBySaleStatus(unitsBySaleStatus)
                .unitsByPhase(unitsByPhase)
                .averageCompletionPercentage(averageCompletion)
                .totalGrossArea(totalGrossArea)
                .totalNetArea(totalNetArea)
                .totalSalesValue(totalSalesValue != null ? totalSalesValue : BigDecimal.ZERO)
                .totalCollectedPayments(totalCollectedPayments != null ? totalCollectedPayments : BigDecimal.ZERO)
                .totalRemainingPayments(totalRemainingPayments != null ? totalRemainingPayments : BigDecimal.ZERO)
                .averageQualityScore(averageQualityScore)
                .build();
    }

    @Transactional(readOnly = true)
    public BuildingDetailsDto getBuildingDetails(String buildingId) {
        Unit building = unitRepository.findById(buildingId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Building not found"));

        List<UnitDto> floorSchemas = getBuildingFloorPlans(buildingId);
        List<UnitDto> subUnits = getUnitsByBuilding(buildingId);

        return BuildingDetailsDto.builder()
                .building(toDto(building))
                .floorSchemas(floorSchemas)
                .subUnits(subUnits)
                .build();
    }


    private boolean matchesSearchCriteria(Unit unit, UnitSearchRequest request) {
        if (request.getKeyword() != null &&
                !unit.getUnitNumber().toLowerCase().contains(request.getKeyword().toLowerCase()) &&
                (unit.getUnitName() == null || !unit.getUnitName().toLowerCase().contains(request.getKeyword().toLowerCase()))) {
            return false;
        }
        if (request.getBuildingId() != null && !request.getBuildingId().equals(unit.getBuildingId())) return false;
        if (request.getBlockName() != null && !request.getBlockName().equals(unit.getBlockName())) return false;
        if (request.getType() != null && request.getType() != unit.getType()) return false;
        if (request.getStatus() != null && request.getStatus() != unit.getStatus()) return false;
        if (request.getSaleStatus() != null && request.getSaleStatus() != unit.getSaleStatus()) return false;
        if (request.getCurrentPhase() != null && request.getCurrentPhase() != unit.getCurrentPhase()) return false;
        if (request.getMinFloor() != null && (unit.getFloor() == null || unit.getFloor() < request.getMinFloor())) return false;
        if (request.getMaxFloor() != null && (unit.getFloor() == null || unit.getFloor() > request.getMaxFloor())) return false;
        if (request.getMinGrossArea() != null && (unit.getGrossArea() == null || unit.getGrossArea().compareTo(request.getMinGrossArea()) < 0)) return false;
        if (request.getMaxGrossArea() != null && (unit.getGrossArea() == null || unit.getGrossArea().compareTo(request.getMaxGrossArea()) > 0)) return false;
        if (request.getMinNetArea() != null && (unit.getNetArea() == null || unit.getNetArea().compareTo(request.getMinNetArea()) < 0)) return false;
        if (request.getMaxNetArea() != null && (unit.getNetArea() == null || unit.getNetArea().compareTo(request.getMaxNetArea()) > 0)) return false;
        if (request.getMinRoomCount() != null && (unit.getRoomCount() == null || unit.getRoomCount() < request.getMinRoomCount())) return false;
        if (request.getMaxRoomCount() != null && (unit.getRoomCount() == null || unit.getRoomCount() > request.getMaxRoomCount())) return false;
        if (request.getMinBedroomCount() != null && (unit.getBedroomCount() == null || unit.getBedroomCount() < request.getMinBedroomCount())) return false;
        if (request.getMaxBedroomCount() != null && (unit.getBedroomCount() == null || unit.getBedroomCount() > request.getMaxBedroomCount())) return false;
        if (request.getDirection() != null && request.getDirection() != unit.getDirection()) return false;
        if (request.getMinPrice() != null && (unit.getListPrice() == null || unit.getListPrice().compareTo(request.getMinPrice()) < 0)) return false;
        if (request.getMaxPrice() != null && (unit.getListPrice() == null || unit.getListPrice().compareTo(request.getMaxPrice()) > 0)) return false;
        if (request.getMinCompletionPercentage() != null && (unit.getCompletionPercentage() == null || unit.getCompletionPercentage() < request.getMinCompletionPercentage())) return false;
        if (request.getMaxCompletionPercentage() != null && (unit.getCompletionPercentage() == null || unit.getCompletionPercentage() > request.getMaxCompletionPercentage())) return false;
        if (request.getHasGarden() != null && !request.getHasGarden().equals(unit.getHasGarden())) return false;
        if (request.getHasTerrace() != null && !request.getHasTerrace().equals(unit.getHasTerrace())) return false;
        if (request.getIsSmartHome() != null && !request.getIsSmartHome().equals(unit.getIsSmartHome())) return false;
        if (request.getHasParkingSpace() != null && !request.getHasParkingSpace().equals(unit.getHasParkingSpace())) return false;
        if (request.getConstructionStartAfter() != null && (unit.getConstructionStartDate() == null || unit.getConstructionStartDate().isBefore(request.getConstructionStartAfter()))) return false;
        if (request.getConstructionStartBefore() != null && (unit.getConstructionStartDate() == null || unit.getConstructionStartDate().isAfter(request.getConstructionStartBefore()))) return false;
        if (request.getEstimatedCompletionAfter() != null && (unit.getEstimatedCompletionDate() == null || unit.getEstimatedCompletionDate().isBefore(request.getEstimatedCompletionAfter()))) return false;
        if (request.getEstimatedCompletionBefore() != null && (unit.getEstimatedCompletionDate() == null || unit.getEstimatedCompletionDate().isAfter(request.getEstimatedCompletionBefore()))) return false;
        return true;
    }

    private Map<String, Long> convertToMap(List<Object[]> results) {
        if (results == null || results.isEmpty()) {
            return new HashMap<>();
        }

        return results.stream()
                .filter(arr -> {
                    if (arr == null || arr.length < 2) {
                        log.warn("Invalid array in convertToMap: array is null or has fewer than 2 elements");
                        return false;
                    }
                    if (arr[0] == null) {
                        log.warn("Null key in convertToMap at index 0, skipping entry");
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> arr[1] != null ? ((Number) arr[1]).longValue() : 0L,
                        (existing, replacement) -> replacement  // Handle duplicate keys
                ));
    }

    @Transactional(readOnly = true)
    public List<UnitMapGeometry> getMapGeometry(String projectId) {

        List<Unit> units = unitRepository.findByProjectIdAndDeletedAtIsNull(projectId);

        return units.stream()
                .map(unit -> UnitMapGeometry.builder()
                        .id(unit.getId())
                        .parentUnitId(unit.getParentUnitId())
                        .unitName(unit.getUnitName())
                        .type(unit.getType())
                        .latitude(unit.getLatitude())
                        .longitude(unit.getLongitude())
                        .footprintJson(unit.getFootprintJson())
                        .completionPercentage(unit.getCompletionPercentage())
                        .build()
                )
                .collect(Collectors.toList());
    }


    private UnitDto toDto(Unit unit) {
        return UnitDto.builder()
                .id(unit.getId())
                .unitNumber(unit.getUnitNumber())
                .unitName(unit.getUnitName())
                .description(unit.getDescription())
                .companyId(unit.getCompanyId())
                .projectId(unit.getProjectId())
                .parentUnitId(unit.getParentUnitId())
                .hasSubUnits(unit.getHasSubUnits())
                .footprintJson(unit.getFootprintJson())
                .subUnitsCount(unit.getSubUnitsCount())
                .buildingId(unit.getBuildingId())
                .blockName(unit.getBlockName())
                .floor(unit.getFloor())
                .section(unit.getSection())
                .type(unit.getType())
                .grossArea(unit.getGrossArea())
                .netArea(unit.getNetArea())
                .roomCount(unit.getRoomCount())
                .bedroomCount(unit.getBedroomCount())
                .bathroomCount(unit.getBathroomCount())
                .balconyCount(unit.getBalconyCount())
                .direction(unit.getDirection())
                .hasGarden(unit.getHasGarden())
                .gardenArea(unit.getGardenArea())
                .hasTerrace(unit.getHasTerrace())
                .terraceArea(unit.getTerraceArea())
                .floorPlanJson(unit.getFloorPlanJson())
                .floorPlanImageUrl(unit.getFloorPlanImageUrl())
                .floorPlanWidth(unit.getFloorPlanWidth())
                .floorPlanLength(unit.getFloorPlanLength())
                .ceilingHeight(unit.getCeilingHeight())
                .status(unit.getStatus())
                .completionPercentage(unit.getCompletionPercentage())
                .currentPhase(unit.getCurrentPhase())
                .constructionStartDate(unit.getConstructionStartDate())
                .estimatedCompletionDate(unit.getEstimatedCompletionDate())
                .actualCompletionDate(unit.getActualCompletionDate())
                .saleStatus(unit.getSaleStatus())
                .ownerId(unit.getOwnerId())
                .ownerName(unit.getOwnerName())
                .ownerEmail(unit.getOwnerEmail())
                .ownerPhone(unit.getOwnerPhone())
                .reservationDate(unit.getReservationDate())
                .saleDate(unit.getSaleDate())
                .deliveryDate(unit.getDeliveryDate())
                .listPrice(unit.getListPrice())
                .salePrice(unit.getSalePrice())
                .currency(unit.getCurrency())
                .pricePerSquareMeter(unit.getPricePerSquareMeter())
                .totalPaid(unit.getTotalPaid())
                .remainingPayment(unit.getRemainingPayment())
                .paymentPercentage(unit.getPaymentPercentage())
                .qualityScore(unit.getQualityScore())
                .hasDefects(unit.getHasDefects())
                .defectCount(unit.getDefectCount())
                .features(unit.getFeatures())
                .documentIds(unit.getDocumentIds())
                .imageUrls(unit.getImageUrls())
                .videoUrls(unit.getVideoUrls())
                .virtualTourUrl(unit.getVirtualTourUrl())
                .latitude(unit.getLatitude())
                .longitude(unit.getLongitude())
                .workItems(getWorkItems(unit.getId()))
                .notifyOwnerOnProgress(unit.getNotifyOwnerOnProgress())
                .notifyOwnerOnCompletion(unit.getNotifyOwnerOnCompletion())
                .tags(unit.getTags())
                .energyCertificate(unit.getEnergyCertificate())
                .isSmartHome(unit.getIsSmartHome())
                .hasParkingSpace(unit.getHasParkingSpace())
                .parkingNumber(unit.getParkingNumber())
                .createdAt(unit.getCreatedAt())
                .updatedAt(unit.getUpdatedAt())
                .viewCount(unit.getViewCount())
                .notes(unit.getNotes())
                .build();
    }

    private UnitWorkItemDto toWorkItemDto(UnitWorkItem workItem) {
        return UnitWorkItemDto.builder()
                .id(workItem.getId())
                .unitId(workItem.getUnitId())
                .workName(workItem.getWorkName())
                .description(workItem.getDescription())
                .status(workItem.getStatus())
                .completionPercentage(workItem.getCompletionPercentage())
                .weightPercentage(workItem.getWeightPercentage())
                .startDate(workItem.getStartDate())
                .dueDate(workItem.getDueDate())
                .completedAt(workItem.getCompletedAt())
                .assignedContractorId(workItem.getAssignedContractorId())
                .assignedWorkerId(workItem.getAssignedWorkerId())
                .taskId(workItem.getTaskId())
                .createdAt(workItem.getCreatedAt())
                .updatedAt(workItem.getUpdatedAt())
                .build();
    }

    private void publishUnitCreatedEvent(Unit unit) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "unit.created");
        event.put("unitId", unit.getId());
        event.put("unitNumber", unit.getUnitNumber());
        event.put("companyId", unit.getCompanyId());
        event.put("projectId", unit.getProjectId());
        event.put("type", unit.getType());
        event.put("timestamp", LocalDateTime.now());
        kafkaTemplate.send("unit.created", event);
        log.info("Unit created event published: {}", unit.getId());
    }

    private void publishUnitUpdatedEvent(Unit unit) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "unit.updated");
        event.put("unitId", unit.getId());
        event.put("companyId", unit.getCompanyId());
        event.put("projectId", unit.getProjectId());
        event.put("timestamp", LocalDateTime.now());
        kafkaTemplate.send("unit.updated", event);
        log.info("Unit updated event published: {}", unit.getId());
    }

    private void publishUnitStatusChangedEvent(Unit unit, UnitStatus oldStatus) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "unit.status.changed");
        event.put("unitId", unit.getId());
        event.put("unitNumber", unit.getUnitNumber());
        event.put("companyId", unit.getCompanyId());
        event.put("projectId", unit.getProjectId());
        event.put("oldStatus", oldStatus);
        event.put("newStatus", unit.getStatus());
        event.put("timestamp", LocalDateTime.now());
        kafkaTemplate.send("unit.status.changed", event);
        log.info("Unit status changed event published: {} -> {}", oldStatus, unit.getStatus());
    }

    private void publishUnitProgressUpdatedEvent(Unit unit, Integer oldPercentage) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "unit.progress.updated");
        event.put("unitId", unit.getId());
        event.put("unitNumber", unit.getUnitNumber());
        event.put("companyId", unit.getCompanyId());
        event.put("projectId", unit.getProjectId());
        event.put("ownerId", unit.getOwnerId());
        event.put("oldPercentage", oldPercentage);
        event.put("newPercentage", unit.getCompletionPercentage());
        event.put("currentPhase", unit.getCurrentPhase());
        event.put("timestamp", LocalDateTime.now());
        kafkaTemplate.send("unit.progress.updated", event);
        log.info("Unit progress updated event published: {}%", unit.getCompletionPercentage());
    }

    private void publishUnitReservedEvent(Unit unit) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "unit.reserved");
        event.put("unitId", unit.getId());
        event.put("unitNumber", unit.getUnitNumber());
        event.put("companyId", unit.getCompanyId());
        event.put("projectId", unit.getProjectId());
        event.put("ownerId", unit.getOwnerId());
        event.put("ownerName", unit.getOwnerName());
        event.put("timestamp", LocalDateTime.now());
        kafkaTemplate.send("unit.reserved", event);
        log.info("Unit reserved event published: {}", unit.getId());
    }

    private void publishUnitSoldEvent(Unit unit) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "unit.sold");
        event.put("unitId", unit.getId());
        event.put("unitNumber", unit.getUnitNumber());
        event.put("companyId", unit.getCompanyId());
        event.put("projectId", unit.getProjectId());
        event.put("ownerId", unit.getOwnerId());
        event.put("ownerName", unit.getOwnerName());
        event.put("salePrice", unit.getSalePrice());
        event.put("timestamp", LocalDateTime.now());
        kafkaTemplate.send("unit.sold", event);
        log.info("Unit sold event published: {}", unit.getId());
    }

    private void publishUnitReservationCancelledEvent(Unit unit) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "unit.reservation.cancelled");
        event.put("unitId", unit.getId());
        event.put("unitNumber", unit.getUnitNumber());
        event.put("companyId", unit.getCompanyId());
        event.put("projectId", unit.getProjectId());
        event.put("timestamp", LocalDateTime.now());
        kafkaTemplate.send("unit.reservation.cancelled", event);
        log.info("Unit reservation cancelled event published: {}", unit.getId());
    }

    private void publishUnitDeletedEvent(Unit unit, boolean hardDelete) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "unit.deleted");
        event.put("unitId", unit.getId());
        event.put("unitNumber", unit.getUnitNumber());
        event.put("companyId", unit.getCompanyId());
        event.put("projectId", unit.getProjectId());
        event.put("hardDelete", hardDelete);
        event.put("timestamp", LocalDateTime.now());
        kafkaTemplate.send("unit.deleted", event);
        log.info("Unit deleted event published: {}", unit.getId());
    }

    private void publishWorkItemCreatedEvent(UnitWorkItem workItem, Unit unit) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "unit.workitem.created");
        event.put("workItemId", workItem.getId());
        event.put("unitId", workItem.getUnitId());
        event.put("workName", workItem.getWorkName());
        event.put("companyId", unit.getCompanyId());
        event.put("projectId", unit.getProjectId());
        event.put("timestamp", LocalDateTime.now());
        kafkaTemplate.send("unit.workitem.created", event);
        log.info("Work item created event published: {}", workItem.getId());
    }

    private void publishWorkItemUpdatedEvent(UnitWorkItem workItem, Integer oldPercentage) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "unit.workitem.updated");
        event.put("workItemId", workItem.getId());
        event.put("unitId", workItem.getUnitId());
        event.put("workName", workItem.getWorkName());
        event.put("oldPercentage", oldPercentage);
        event.put("newPercentage", workItem.getCompletionPercentage());
        event.put("status", workItem.getStatus());
        event.put("timestamp", LocalDateTime.now());
        kafkaTemplate.send("unit.workitem.updated", event);
        log.info("Work item updated event published: {}", workItem.getId());
    }

    private void publishOwnerNotificationEvent(Unit unit, String notificationType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "unit.owner.notification");
        event.put("notificationType", notificationType);
        event.put("unitId", unit.getId());
        event.put("unitNumber", unit.getUnitNumber());
        event.put("ownerId", unit.getOwnerId());
        event.put("ownerName", unit.getOwnerName());
        event.put("ownerEmail", unit.getOwnerEmail());
        event.put("completionPercentage", unit.getCompletionPercentage());
        event.put("currentPhase", unit.getCurrentPhase());
        event.put("timestamp", LocalDateTime.now());
        kafkaTemplate.send("unit.owner.notification", event);
        log.info("Owner notification event published: {} - {}", unit.getOwnerId(), notificationType);
    }

    // UnitService.java
    @Transactional(readOnly = true)
    public List<UnitDto> getBuildingFloorPlans(String buildingId) {
        // ✅ Sadece floor schema'ları getir
        return unitRepository.findFloorSchemas(buildingId)
                .stream()
                .map(this::toDto)
                .sorted((a, b) -> Integer.compare(a.getFloor(), b.getFloor()))
                .collect(Collectors.toList());
    }

    public List<String> getUnitIdsByProjectId(String projectId) {
        log.info("📋 Fetching unit IDs for project: {}", projectId);

        List<String> unitIds = unitRepository.findByProjectId(projectId)
                .stream()
                .map(Unit::getId)
                .collect(Collectors.toList());

        log.info("✅ Found {} unit IDs for project {}", unitIds.size(), projectId);
        return unitIds;
    }
}