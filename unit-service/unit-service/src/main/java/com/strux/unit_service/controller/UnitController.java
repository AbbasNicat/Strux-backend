package com.strux.unit_service.controller;

import com.strux.unit_service.dto.*;
import com.strux.unit_service.enums.*;
import com.strux.unit_service.service.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
@Slf4j
public class UnitController {

    private final UnitService unitService;

    @GetMapping("/project/{projectId}/count")
    public ResponseEntity<Long> countUnitsByProject(@PathVariable String projectId) {
        Long count = unitService.getUnitCount(projectId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/project/{projectId}/map-info")
    public ResponseEntity<List<UnitMapInfo>> getProjectUnitsForMap(@PathVariable String projectId) {
        log.info("📍 Fetching map info for project units: {}", projectId);
        List<UnitMapInfo> mapInfo = unitService.getProjectUnitsForMap(projectId);
        return ResponseEntity.ok(mapInfo);
    }

    @GetMapping("/map/{projectId}")
    public ResponseEntity<List<UnitMapGeometry>> getMapGeometry(@PathVariable String projectId) {
        return ResponseEntity.ok(unitService.getMapGeometry(projectId));
    }

    @GetMapping("/project/{projectId}/ids")
    public ResponseEntity<List<String>> getProjectUnitIds(@PathVariable String projectId) {
        log.info("📋 Getting unit IDs for project: {}", projectId);

        List<String> unitIds = unitService.getUnitIdsByProjectId(projectId);

        log.info("✅ Found {} unit IDs for project {}", unitIds.size(), projectId);
        return ResponseEntity.ok(unitIds);
    }

    @PostMapping
    public ResponseEntity<UnitDto> createUnit(@RequestBody @Valid UnitCreateRequest request) {
        log.info("Creating unit: {} for project: {}", request.getUnitNumber(), request.getProjectId());
        UnitDto unit = unitService.createUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(unit);
    }

    @GetMapping("/{unitId}")
    public ResponseEntity<UnitDto> getUnit(@PathVariable String unitId) {
        UnitDto unit = unitService.getUnit(unitId);
        return ResponseEntity.ok(unit);
    }

    @GetMapping("/{buildingId}/floor-plans")
    public ResponseEntity<List<UnitDto>> getBuildingFloorPlans(@PathVariable String buildingId) {
        List<UnitDto> floorPlans = unitService.getBuildingFloorPlans(buildingId);
        return ResponseEntity.ok(floorPlans);
    }

    // ✅ NEW: Building'in tüm detaylarını getir (floor schemas + sub-units ayrı ayrı)
    @GetMapping("/{buildingId}/details")
    public ResponseEntity<BuildingDetailsDto> getBuildingDetails(@PathVariable String buildingId) {
        BuildingDetailsDto details = unitService.getBuildingDetails(buildingId);
        return ResponseEntity.ok(details);
    }

    // ✅ FIX: Sadece gerçek unit'leri getir (floor schema'lar hariç)
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<UnitDto>> getUnitsByProject(@PathVariable String projectId) {
        List<UnitDto> units = unitService.getUnitsByProject(projectId);
        return ResponseEntity.ok(units);
    }

    // ✅ FIX: Sadece sub-unit'leri getir (floor schema'lar hariç)
    @GetMapping("/building/{buildingId}")
    public ResponseEntity<List<UnitDto>> getUnitsByBuilding(@PathVariable String buildingId) {
        List<UnitDto> units = unitService.getUnitsByBuilding(buildingId);
        return ResponseEntity.ok(units);
    }

    @GetMapping("/project/{projectId}/block/{blockName}")
    public ResponseEntity<List<UnitDto>> getUnitsByBlock(
            @PathVariable String projectId,
            @PathVariable String blockName
    ) {
        List<UnitDto> units = unitService.getUnitsByBlock(projectId, blockName);
        return ResponseEntity.ok(units);
    }

    @GetMapping("/project/{projectId}/floor/{floor}")
    public ResponseEntity<List<UnitDto>> getUnitsByFloor(
            @PathVariable String projectId,
            @PathVariable Integer floor
    ) {
        List<UnitDto> units = unitService.getUnitsByFloor(projectId, floor);
        return ResponseEntity.ok(units);
    }

    @GetMapping("/project/{projectId}/status/{status}")
    public ResponseEntity<List<UnitDto>> getUnitsByStatus(
            @PathVariable String projectId,
            @PathVariable UnitStatus status
    ) {
        List<UnitDto> units = unitService.getUnitsByStatus(projectId, status);
        return ResponseEntity.ok(units);
    }

    @GetMapping("/project/{projectId}/sale-status/{saleStatus}")
    public ResponseEntity<List<UnitDto>> getUnitsBySaleStatus(
            @PathVariable String projectId,
            @PathVariable SaleStatus saleStatus
    ) {
        List<UnitDto> units = unitService.getUnitsBySaleStatus(projectId, saleStatus);
        return ResponseEntity.ok(units);
    }

    @GetMapping("/project/{projectId}/type/{type}")
    public ResponseEntity<List<UnitDto>> getUnitsByType(
            @PathVariable String projectId,
            @PathVariable UnitType type
    ) {
        List<UnitDto> units = unitService.getUnitsByType(projectId, type);
        return ResponseEntity.ok(units);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<UnitDto>> getUnitsByOwner(@PathVariable String ownerId) {
        List<UnitDto> units = unitService.getUnitsByOwner(ownerId);
        return ResponseEntity.ok(units);
    }

    @GetMapping("/project/{projectId}/available")
    public ResponseEntity<List<UnitDto>> getAvailableUnits(@PathVariable String projectId) {
        List<UnitDto> units = unitService.getAvailableUnits(projectId);
        return ResponseEntity.ok(units);
    }

    @GetMapping("/project/{projectId}/overdue")
    public ResponseEntity<List<UnitDto>> getOverdueUnits(@PathVariable String projectId) {
        List<UnitDto> units = unitService.getOverdueUnits(projectId);
        return ResponseEntity.ok(units);
    }

    @PostMapping("/search")
    public ResponseEntity<List<UnitDto>> searchUnits(@RequestBody UnitSearchRequest request) {
        List<UnitDto> units = unitService.searchUnits(request);
        return ResponseEntity.ok(units);
    }

    @PutMapping("/{unitId}")
    public ResponseEntity<UnitDto> updateUnit(
            @PathVariable String unitId,
            @RequestBody @Valid UnitUpdateRequest request
    ) {
        UnitDto unit = unitService.updateUnit(unitId, request);
        return ResponseEntity.ok(unit);
    }

    @PutMapping("/{unitId}/progress")
    public ResponseEntity<UnitDto> updateProgress(
            @PathVariable String unitId,
            @RequestBody @Valid UnitProgressUpdateRequest request
    ) {
        UnitDto unit = unitService.updateProgress(unitId, request);
        return ResponseEntity.ok(unit);
    }

    @PutMapping("/{unitId}/reserve")
    public ResponseEntity<UnitDto> reserveUnit(
            @PathVariable String unitId,
            @RequestBody @Valid UnitReservationRequest request
    ) {
        UnitDto unit = unitService.reserveUnit(unitId, request);
        return ResponseEntity.ok(unit);
    }

    @PutMapping("/{unitId}/sell")
    public ResponseEntity<UnitDto> sellUnit(
            @PathVariable String unitId,
            @RequestBody @Valid UnitSaleRequest request
    ) {
        UnitDto unit = unitService.sellUnit(unitId, request);
        return ResponseEntity.ok(unit);
    }

    @PutMapping("/{unitId}/cancel-reservation")
    public ResponseEntity<UnitDto> cancelReservation(@PathVariable String unitId) {
        UnitDto unit = unitService.cancelReservation(unitId);
        return ResponseEntity.ok(unit);
    }

    @DeleteMapping("/{unitId}")
    public ResponseEntity<Void> deleteUnit(
            @PathVariable String unitId,
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        unitService.deleteUnit(unitId, hardDelete);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{unitId}/work-items")
    public ResponseEntity<UnitWorkItemDto> addWorkItem(
            @PathVariable String unitId,
            @RequestBody @Valid WorkItemCreateRequest request
    ) {
        UnitWorkItemDto workItem = unitService.addWorkItem(unitId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(workItem);
    }

    @GetMapping("/{unitId}/work-items")
    public ResponseEntity<List<UnitWorkItemDto>> getWorkItems(@PathVariable String unitId) {
        List<UnitWorkItemDto> workItems = unitService.getWorkItems(unitId);
        return ResponseEntity.ok(workItems);
    }

    @PutMapping("/work-items/{workItemId}")
    public ResponseEntity<UnitWorkItemDto> updateWorkItem(
            @PathVariable String workItemId,
            @RequestBody @Valid WorkItemUpdateRequest request
    ) {
        UnitWorkItemDto workItem = unitService.updateWorkItem(workItemId, request);
        return ResponseEntity.ok(workItem);
    }

    @DeleteMapping("/work-items/{workItemId}")
    public ResponseEntity<Void> deleteWorkItem(@PathVariable String workItemId) {
        unitService.deleteWorkItem(workItemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/project/{projectId}/stats")
    public ResponseEntity<UnitStatsResponse> getUnitStats(@PathVariable String projectId) {
        UnitStatsResponse stats = unitService.getUnitStats(projectId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{unitId}/workers")
    public ResponseEntity<List<UserResponse>> getUnitWorkers(@PathVariable String unitId) {
        log.info("GET /api/units/{}/workers", unitId);
        List<UserResponse> workers = unitService.getUnitWorkers(unitId);
        return ResponseEntity.ok(workers);
    }

    @GetMapping("/{unitId}/project-id")
    public ResponseEntity<String> getProjectIdByUnit(@PathVariable String unitId) {
        log.info("Fetching projectId for unit: {}", unitId);
        UnitDto unit = unitService.getUnit(unitId);
        return ResponseEntity.ok(unit.getProjectId());
    }

    @GetMapping("/{unitId}/name")
    public ResponseEntity<String> getUnitName(@PathVariable String unitId) {
        log.info("Fetching unit name for: {}", unitId);
        UnitDto unit = unitService.getUnit(unitId);
        String name = unit.getUnitName() != null ? unit.getUnitName() : "Unit " + unit.getUnitNumber();
        return ResponseEntity.ok(name);
    }
}