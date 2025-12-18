package com.strux.user_service.controller;

import com.strux.user_service.dto.*;
import com.strux.user_service.enums.WorkerSpecialty;
import com.strux.user_service.service.WorkerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WorkerController {

    private final WorkerService workerService;

    @GetMapping("/company/{companyId}/workers/count")
    public ResponseEntity<Long> getCompanyWorkerCount(@PathVariable String companyId) {
        log.info("📊 Getting worker count for company: {}", companyId);

        Long count = workerService.countWorkersByCompany(companyId);

        log.info("✅ Company {} has {} workers", companyId, count);
        return ResponseEntity.ok(count);
    }

    // ✅ Worker'ın unit'lerini getir (ID listesi) - String parametresi
    @GetMapping("/{workerId}/units")
    public ResponseEntity<List<String>> getWorkerUnits(@PathVariable String workerId) {
        List<String> unitIds = workerService.getWorkerUnitIds(workerId);
        return ResponseEntity.ok(unitIds);
    }

    @GetMapping("/project/{projectId}/stats")
    public ResponseEntity<ProjectWorkerStatsResponse> getProjectWorkerStats(@PathVariable String projectId) {
        Long totalWorkers = workerService.countWorkersByProject(projectId);
        return ResponseEntity.ok(new ProjectWorkerStatsResponse(totalWorkers));
    }

    // ✅ Worker'ın ilk unit'ini getir
    @GetMapping("/{workerId}/current-unit")
    public ResponseEntity<String> getWorkerCurrentUnit(@PathVariable String workerId) {
        String unitId = workerService.getWorkerFirstUnitId(workerId);
        if (unitId == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(unitId);
    }

    // ✅ Worker'ın unit detaylarını getir (Set döner)
    @GetMapping("/{workerId}/unit-details")
    public ResponseEntity<Set<String>> getWorkerUnitDetails(@PathVariable String workerId) {
        Set<String> units = workerService.getWorkerUnits(UUID.fromString(workerId));
        return ResponseEntity.ok(units);
    }

    // ✅ Worker'ı unit'e assign et
    @PostMapping("/{workerId}/assign-unit")
    public ResponseEntity<UserResponse> assignWorkerToUnit(
            @PathVariable String workerId,
            @RequestParam String unitId,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        UserResponse response = workerService.assignWorkerToUnit(
                UUID.fromString(workerId),
                unitId,
                userId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ✅ Worker'ı unit'ten çıkar
    @DeleteMapping("/{workerId}/remove-unit/{unitId}")
    public ResponseEntity<UserResponse> removeWorkerFromUnit(
            @PathVariable String workerId,
            @PathVariable String unitId,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        UserResponse response = workerService.removeWorkerFromUnit(
                UUID.fromString(workerId),
                unitId,
                userId
        );
        return ResponseEntity.ok(response);
    }

    // ✅ Unit'e atanmış worker'ları getir
    @GetMapping("/by-unit/{unitId}")
    public ResponseEntity<List<UserResponse>> getWorkersByUnit(@PathVariable String unitId) {
        List<UserResponse> workers = workerService.getWorkersByUnit(unitId);
        return ResponseEntity.ok(workers);
    }

    // ✅ Worker'ın projelerini getir
    @GetMapping("/{workerId}/projects")
    public ResponseEntity<List<ProjectResponse>> getWorkerProjects(@PathVariable String workerId) {
        List<ProjectResponse> projects = workerService.getWorkerProjects(workerId);
        return ResponseEntity.ok(projects);
    }

    // ✅ Worker'ı projeden çıkar
    @DeleteMapping("/{workerId}/projects/{projectId}")
    public ResponseEntity<Void> removeWorkerFromProject(
            @PathVariable String workerId,
            @PathVariable String projectId,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        workerService.removeWorkerFromProject(UUID.fromString(workerId), projectId, userId);
        return ResponseEntity.noContent().build();
    }

    // ✅ Worker'ı projeye ekle
    @PostMapping("/{workerId}/projects/{projectId}")
    public ResponseEntity<Void> addWorkerToProject(
            @PathVariable String workerId,
            @PathVariable String projectId,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        workerService.addWorkerToProject(UUID.fromString(workerId), projectId, userId);
        return ResponseEntity.ok().build();
    }

    // ✅ Worker availability güncelle
    @PutMapping("/{workerId}/availability")
    public ResponseEntity<UserResponse> updateWorkerAvailability(
            @PathVariable String workerId,
            @RequestParam boolean isAvailable,
            @RequestParam(required = false) LocalDate availableFrom,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        UserResponse response = workerService.updateWorkerAvailability(
                UUID.fromString(workerId),
                isAvailable,
                availableFrom,
                userId
        );
        return ResponseEntity.ok(response);
    }

    // ✅ Worker performance güncelle
    @PostMapping("/{workerId}/performance")
    public ResponseEntity<Void> updateWorkerPerformance(
            @PathVariable String workerId,
            @RequestParam boolean onTime,
            @RequestParam BigDecimal taskRating,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        workerService.updateWorkerPerformance(
                UUID.fromString(workerId),
                onTime,
                taskRating,
                userId
        );
        return ResponseEntity.ok().build();
    }

    // ✅ Worker istatistiklerini getir
    @GetMapping("/{workerId}/stats")
    public ResponseEntity<WorkerStatsResponse> getWorkerStats(@PathVariable String workerId) {
        WorkerStatsResponse stats = workerService.getWorkerStats(UUID.fromString(workerId));
        return ResponseEntity.ok(stats);
    }

    // ✅ Company worker'larını getir
    @GetMapping("/company/{companyId}")
    public ResponseEntity<Page<UserResponse>> getCompanyWorkers(
            @PathVariable String companyId,
            Pageable pageable
    ) {
        Page<UserResponse> workers = workerService.getCompanyWorkers(companyId, pageable);
        return ResponseEntity.ok(workers);
    }

    // ✅ Worker'ları ara
    @GetMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchWorkers(
            @RequestParam(required = false) WorkerSpecialty specialty,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(required = false) BigDecimal minRating,
            Pageable pageable
    ) {
        Page<UserResponse> workers = workerService.searchWorkers(
                specialty,
                city,
                isAvailable,
                minRating,
                pageable
        );
        return ResponseEntity.ok(workers);
    }

    // ✅ Available worker'ları getir
    @GetMapping("/available")
    public ResponseEntity<Page<UserResponse>> getAvailableWorkers(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) WorkerSpecialty specialty,
            Pageable pageable
    ) {
        Page<UserResponse> workers = workerService.getAvailableWorkers(city, specialty, pageable);
        return ResponseEntity.ok(workers);
    }

    // ✅ Top worker'ları getir
    @GetMapping("/top")
    public ResponseEntity<List<UserResponse>> getTopWorkersBySpecialty(
            @RequestParam WorkerSpecialty specialty,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<UserResponse> workers = workerService.getTopWorkersBySpecialty(specialty, limit);
        return ResponseEntity.ok(workers);
    }
}