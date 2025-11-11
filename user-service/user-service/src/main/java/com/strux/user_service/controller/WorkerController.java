package com.strux.user_service.controller;

import com.strux.user_service.config.SecurityUtils;
import com.strux.user_service.dto.*;
import com.strux.user_service.enums.WorkerSpecialty;
import com.strux.user_service.service.UserService;
import com.strux.user_service.service.WorkerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
@Slf4j
public class WorkerController {

    private final WorkerService workerService;
    private final UserService userService;
    private final SecurityUtils securityUtils;

//    @PostMapping("/register")
//    public ResponseEntity<UserResponse> registerWorker(
//            @Valid @RequestBody WorkerRegistrationRequest request) {
//
//        log.info("POST /api/users/workers/register - email: {}", request.getEmail());
//
//        UserResponse response = userService.registerWorker(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }

    @DeleteMapping("/{workerId}/company")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'MANAGER')")
    public ResponseEntity<Void> removeWorkerFromCompany(
            @PathVariable UUID workerId,
            @RequestParam String companyId,
            @RequestHeader(value = "X-User-Id", required = false) String removedBy) {

        log.info("DELETE /api/workers/{}/company - Removing from company: {}", workerId, companyId);

        try {
            // Security check: User must be from the same company
            String currentUserCompanyId = securityUtils.getCurrentUserCompanyId();

            if (!currentUserCompanyId.equals(companyId)) {
                log.warn("⚠️ User from company {} tried to remove employee from company {}",
                        currentUserCompanyId, companyId);
                throw new SecurityException("Cannot remove employees from other companies");
            }

            workerService.removeEmployeeFromCompany(companyId, workerId.toString(), removedBy);

            log.info("✅ Worker {} removed from company {}", workerId, companyId);
            return ResponseEntity.noContent().build();

        } catch (SecurityException e) {
            log.error("❌ Security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("❌ Error removing worker from company: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchWorkers(
            @RequestParam(required = false) WorkerSpecialty specialty,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(required = false) BigDecimal minRating,
            Pageable pageable) {

        log.info("GET /api/workers/search - specialty: {}, city: {}, available: {}, minRating: {}",
                specialty, city, isAvailable, minRating);

        Page<UserResponse> response = workerService.searchWorkers(
                specialty, city, isAvailable, minRating, pageable);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'WORKER')") // ✅ Worker de görebilir
    public ResponseEntity<Page<UserResponse>> getCompanyWorkers(
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            String currentUserCompanyId = securityUtils.getCurrentUserCompanyId();

            log.info("Fetching company workers - Requested: {}, Current User's Company: {}",
                    companyId, currentUserCompanyId);

            if (!currentUserCompanyId.equals(companyId)) {
                log.warn("⚠️ User from company {} tried to access company {}",
                        currentUserCompanyId, companyId);
                throw new SecurityException("Cannot access other company's workers");
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<UserResponse> workers = workerService.getCompanyWorkers(companyId, pageable);

            log.info("✅ Found {} workers for company {}", workers.getTotalElements(), companyId);
            return ResponseEntity.ok(workers);

        } catch (SecurityException e) {
            log.error("❌ Security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("❌ Error fetching company workers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{workerId}/projects")
    public ResponseEntity<List<ProjectResponse>> getWorkerProjects(@PathVariable String workerId) {
        log.info("GET /api/workers/{}/projects", workerId);
        List<ProjectResponse> projects = workerService.getWorkerProjects(workerId);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/company/{companyId}/all")
    public ResponseEntity<Page<UserResponse>> getAllCompanyEmployees(
            @PathVariable String companyId,
            Pageable pageable) {

        log.info("GET /api/workers/company/{}/all", companyId);
        Page<UserResponse> response = workerService.getAllCompanyEmployees(companyId, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{workerId}/availability")
    public ResponseEntity<UserResponse> updateWorkerAvailability(
            @PathVariable UUID workerId,
            @RequestParam boolean isAvailable,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate availableFrom,
            @RequestHeader(value = "X-User-Id", required = false) String updatedBy) {

        log.info("PATCH /api/workers/{}/availability - isAvailable: {}", workerId, isAvailable);

        UserResponse response = workerService.updateWorkerAvailability(
                workerId, isAvailable, availableFrom, updatedBy);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{workerId}/performance")
    public ResponseEntity<Void> updateWorkerPerformance(
            @PathVariable UUID workerId,
            @Valid @RequestBody WorkerPerformanceUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String updatedBy) {

        log.info("POST /api/workers/{}/performance", workerId);

        workerService.updateWorkerPerformance(
                workerId, request.getOnTime(), request.getRating(), updatedBy);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{workerId}/projects/{projectId}")
    public ResponseEntity<Void> addWorkerToProject(
            @PathVariable UUID workerId,
            @PathVariable String projectId,
            @RequestHeader(value = "X-User-Id", required = false) String updatedBy) {

        log.info("POST /api/workers/{}/projects/{}", workerId, projectId);
        workerService.addWorkerToProject(workerId, projectId, updatedBy);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{workerId}/projects/{projectId}")
    public ResponseEntity<Void> removeWorkerFromProject(
            @PathVariable UUID workerId,
            @PathVariable String projectId,
            @RequestHeader(value = "X-User-Id", required = false) String updatedBy) {

        log.info("DELETE /api/workers/{}/projects/{}", workerId, projectId);
        workerService.removeWorkerFromProject(workerId, projectId, updatedBy);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/top")
    public ResponseEntity<List<UserResponse>> getTopWorkers(
            @RequestParam WorkerSpecialty specialty,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("GET /api/workers/top - specialty: {}, limit: {}", specialty, limit);
        List<UserResponse> response = workerService.getTopWorkersBySpecialty(specialty, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{workerId}/stats")
    public ResponseEntity<WorkerStatsResponse> getWorkerStats(@PathVariable UUID workerId) {
        log.info("GET /api/workers/{}/stats", workerId);
        WorkerStatsResponse response = workerService.getWorkerStats(workerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<Page<UserResponse>> getAvailableWorkers(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) WorkerSpecialty specialty,
            Pageable pageable) {

        log.info("GET /api/workers/available - city: {}, specialty: {}", city, specialty);
        Page<UserResponse> response = workerService.getAvailableWorkers(city, specialty, pageable);
        return ResponseEntity.ok(response);
    }
}
