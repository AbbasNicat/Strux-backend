package com.strux.user_service.service;

import com.strux.user_service.dto.*;
import com.strux.user_service.enums.AuditEvent;
import com.strux.user_service.enums.UserRole;
import com.strux.user_service.enums.UserStatus;
import com.strux.user_service.enums.WorkerSpecialty;
import com.strux.user_service.event.WorkerAssignedToProjectEvent;
import com.strux.user_service.event.WorkerAssignedToUnitEvent;
import com.strux.user_service.event.WorkerRemovedFromCompanyEvent;
import com.strux.user_service.event.WorkerRemovedFromUnitEvent;
import com.strux.user_service.exceptions.InvalidInputException;
import com.strux.user_service.exceptions.UserNotFoundException;
import com.strux.user_service.exceptions.UserServiceException;
import com.strux.user_service.mapper.UserMapper;
import com.strux.user_service.model.User;
import com.strux.user_service.model.WorkerProfile;
import com.strux.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkerService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WebClient.Builder webClientBuilder;

    @Value("${services.unit.url:http://localhost:9099}")
    private String unitServiceUrl;

    @Value("${services.project.url:http://localhost:9095}")
    private String projectServiceUrl;

    // ✅ Worker'ın tüm unit ID'lerini getir
    public List<String> getWorkerUnitIds(String workerId) {
        log.debug("Fetching unit IDs for worker: {}", workerId);

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new UserNotFoundException("Worker not found: " + workerId));

        validateWorker(worker);

        WorkerProfile profile = worker.getWorkerProfile();
        if (profile == null || profile.getAssignedUnitIds() == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(profile.getAssignedUnitIds());
    }

    // ✅ Worker'ın ilk unit ID'sini getir (tek unit için)
    public String getWorkerFirstUnitId(String workerId) {
        log.debug("Fetching first unit ID for worker: {}", workerId);

        List<String> unitIds = getWorkerUnitIds(workerId);

        return unitIds.isEmpty() ? null : unitIds.get(0);
    }

    public Long countWorkersByCompany(String companyId) {
        log.info("📊 Counting workers for company: {}", companyId);

        Long count = userRepository.countByCompanyIdAndRole(companyId, UserRole.WORKER);

        log.info("✅ Company {} has {} workers", companyId, count);
        return count;
    }

    public Long countWorkersByProject(String projectId) {
        try {
            log.info("📊 Counting workers for project {}", projectId);

            // ✅ user_active_project_ids tablosundan direkt say
            Long count = userRepository.countWorkersByProjectId(projectId);

            log.info("✅ Total workers assigned to project {} = {}", projectId, count);
            return count;

        } catch (Exception e) {
            log.error("❌ Error counting workers for project {}: {}",
                    projectId, e.getMessage(), e);
            return 0L;
        }
    }

    @Transactional
    public UserResponse assignWorkerToUnit(UUID workerId, String unitId, String assignedBy) {
        try {
            log.info("Assigning worker {} to unit {}", workerId, unitId);

            User worker = userRepository.findById(workerId.toString())
                    .orElseThrow(() -> new UserNotFoundException("Worker not found: " + workerId));

            validateWorker(worker);

            WorkerProfile profile = worker.getWorkerProfile();
            if (profile == null) {
                throw new InvalidInputException("Worker profile not found");
            }

            if (profile.getAssignedUnitIds().contains(unitId)) {
                log.warn("Worker {} already assigned to unit {}", workerId, unitId);
                return userMapper.toResponse(worker);
            }

            profile.getAssignedUnitIds().add(unitId);
            worker.setUpdatedAt(LocalDateTime.now());

            User savedWorker = userRepository.save(worker);

            String unitName = getUnitName(unitId);
            String assignerName = getUserFullName(assignedBy);

            auditLogService.logUserEvent(
                    AuditEvent.USER_UPDATED,
                    worker.getId(),
                    assignedBy,
                    String.format("Worker assigned to unit: %s by %s",
                            unitName != null ? unitName : unitId,
                            assignerName != null ? assignerName : assignedBy)
            );

            publishWorkerAssignedToUnitEvent(savedWorker, unitId, assignedBy);

            log.info("✅ Worker {} assigned to unit {}", workerId, unitId);
            return userMapper.toResponse(savedWorker);

        } catch (UserNotFoundException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error assigning worker to unit: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to assign worker to unit", e);
        }
    }

    @Transactional
    public UserResponse removeWorkerFromUnit(UUID workerId, String unitId, String removedBy) {
        try {
            log.info("Removing worker {} from unit {}", workerId, unitId);

            User worker = userRepository.findById(workerId.toString())
                    .orElseThrow(() -> new UserNotFoundException("Worker not found: " + workerId));

            validateWorker(worker);

            WorkerProfile profile = worker.getWorkerProfile();
            if (profile == null) {
                throw new InvalidInputException("Worker profile not found");
            }

            if (!profile.getAssignedUnitIds().contains(unitId)) {
                log.warn("Worker {} not assigned to unit {}", workerId, unitId);
                return userMapper.toResponse(worker);
            }

            String unitName = getUnitName(unitId);
            String removerName = getUserFullName(removedBy);

            profile.getAssignedUnitIds().remove(unitId);
            worker.setUpdatedAt(LocalDateTime.now());

            User savedWorker = userRepository.save(worker);

            auditLogService.logUserEvent(
                    AuditEvent.USER_UPDATED,
                    worker.getId(),
                    removedBy,
                    String.format("Worker removed from unit: %s by %s",
                            unitName != null ? unitName : unitId,
                            removerName != null ? removerName : removedBy)
            );

            publishWorkerRemovedFromUnitEvent(savedWorker, unitId, removedBy);

            log.info("✅ Worker {} removed from unit {}", workerId, unitId);
            return userMapper.toResponse(savedWorker);

        } catch (UserNotFoundException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error removing worker from unit: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to remove worker from unit", e);
        }
    }

    // ✅ Enhanced: Publish ASSIGN event with names
    private void publishWorkerAssignedToUnitEvent(User worker, String unitId, String assignedBy) {
        try {
            String workerName = worker.getFirstName() + " " + worker.getLastName();
            String unitName = getUnitName(unitId);
            String assignerName = getUserFullName(assignedBy);
            String projectId = getProjectIdFromUnit(unitId);

            WorkerAssignedToUnitEvent event = WorkerAssignedToUnitEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(java.time.OffsetDateTime.now().toString())
                    .workerId(worker.getId())
                    .workerName(workerName)
                    .unitId(unitId)
                    .unitName(unitName != null ? unitName : "Unit " + unitId)
                    .companyId(worker.getCompanyId())
                    .assignedBy(assignedBy)
                    .assignerName(assignerName != null ? assignerName : null)
                    .projectId(projectId)
                    .build();

            kafkaTemplate.send("worker.assigned.to.unit", event);
            log.info("✅ Published worker.assigned.to.unit event: worker={} ({}), unit={} ({}), assigner={} ({})",
                    worker.getId(), workerName, unitId, unitName, assignedBy, assignerName);

        } catch (Exception e) {
            log.warn("Failed to publish worker.assigned.to.unit event: {}", e.getMessage());
        }
    }

    private void publishWorkerRemovedFromUnitEvent(User worker, String unitId, String removedBy) {
        try {
            String workerName = worker.getFirstName() + " " + worker.getLastName();
            String unitName = getUnitName(unitId);
            String removerName = getUserFullName(removedBy);

            WorkerRemovedFromUnitEvent event = WorkerRemovedFromUnitEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(java.time.OffsetDateTime.now().toString())
                    .workerId(worker.getId())
                    .workerName(workerName)
                    .unitId(unitId)
                    .unitName(unitName != null ? unitName : "Unit " + unitId)
                    .companyId(worker.getCompanyId())
                    .removedBy(removedBy)
                    .removerName(removerName != null ? removerName : null)
                    .reason("Removed from unit")
                    .build();

            kafkaTemplate.send("worker.removed.from.unit", event);
            log.info("✅ Published worker.removed.from.unit event: worker={} ({}), unit={} ({}), remover={} ({})",
                    worker.getId(), workerName, unitId, unitName, removedBy, removerName);

        } catch (Exception e) {
            log.warn("Failed to publish worker.removed.from.unit event: {}", e.getMessage());
        }
    }

    // ✅ Helper: Get user full name by ID
    private String getUserFullName(String userIdOrKeycloakId) {
        try {
            if (userIdOrKeycloakId == null || userIdOrKeycloakId.isBlank()) {
                log.warn("getUserFullName called with null/blank ID");
                return null;
            }

            log.debug("Looking up user by ID: {}", userIdOrKeycloakId);

            // Try by User ID first
            Optional<User> userOpt = userRepository.findById(userIdOrKeycloakId);

            // If not found, try by Keycloak ID
            if (userOpt.isEmpty()) {
                log.debug("Not found by User ID, trying Keycloak ID...");
                userOpt = userRepository.findByKeycloakId(userIdOrKeycloakId);
            }

            if (userOpt.isEmpty()) {
                log.warn("❌ User NOT FOUND by User ID or Keycloak ID: {}", userIdOrKeycloakId);
                return null;
            }

            User user = userOpt.get();
            String fullName = user.getFirstName() + " " + user.getLastName();

            log.debug("✅ User found: {} (ID: {}, Keycloak: {})",
                    fullName, user.getId(), user.getKeycloakId());
            return fullName;

        } catch (Exception e) {
            log.error("❌ Error getting user full name for {}: {}", userIdOrKeycloakId, e.getMessage());
            return null;
        }
    }


    // ✅ Helper: Get unit name with fallback
    private String getUnitName(String unitId) {
        try {
            log.debug("Fetching unit name for ID: {}", unitId);

            String token = getAuthToken();
            if (token == null) {
                log.warn("❌ No auth token available for unit lookup");
                return null;
            }

            // ✅ unitServiceUrl (9099) kullanılıyor
            String unitName = webClientBuilder.build()
                    .get()
                    .uri(unitServiceUrl + "/api/units/{unitId}/name", unitId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("✅ Unit name: {}", unitName);
            return unitName;

        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            log.error("❌ Unit Service error for {}: {} - {}",
                    unitId, e.getStatusCode(), e.getMessage());

            if (e.getStatusCode().value() == 404) {
                log.error("   → Unit not found: {}", unitId);
            } else if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                log.error("   → Auth failed - check JWT token");
            }
            return null;

        } catch (Exception e) {
            log.error("❌ Failed to fetch unit name for {}: {}", unitId, e.getMessage());

            if (e.getCause() != null && e.getCause().getMessage() != null) {
                if (e.getCause().getMessage().contains("Connection refused")) {
                    log.error("   → Unit Service not running at: {}", unitServiceUrl);
                }
            }
            return null;
        }
    }

    private String getProjectIdFromUnit(String unitId) {
        try {
            String token = getAuthToken();
            if (token == null) return null;

            // ✅ unitServiceUrl (9099) kullanılıyor
            return webClientBuilder.build()
                    .get()
                    .uri(unitServiceUrl + "/api/units/{unitId}/project-id", unitId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (Exception e) {
            log.warn("Failed to fetch project ID for unit {}: {}", unitId, e.getMessage());
            return null;
        }
    }

    @Transactional(readOnly = true)
    public Set<String> getWorkerUnits(UUID workerId) {
        try {
            log.info("Fetching units for worker {}", workerId);

            User worker = userRepository.findById(workerId.toString())
                    .orElseThrow(() -> new UserNotFoundException("Worker not found: " + workerId));

            validateWorker(worker);

            WorkerProfile profile = worker.getWorkerProfile();
            if (profile == null || profile.getAssignedUnitIds() == null) {
                return new HashSet<>();
            }

            return profile.getAssignedUnitIds();

        } catch (UserNotFoundException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching worker units: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to fetch worker units", e);
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getWorkersByUnit(String unitId) {
        try {
            log.info("Fetching workers for unit {}", unitId);

            List<User> workers = userRepository.findWorkersByUnitId(unitId, UserStatus.ACTIVE);

            log.info("Found {} workers for unit {}", workers.size(), unitId);

            return workers.stream()
                    .map(userMapper::toResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching unit workers: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to fetch unit workers", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getWorkerProjects(String workerId) {
        try {
            log.info("Fetching projects for worker: {}", workerId);

            User worker = userRepository.findById(workerId)
                    .orElseThrow(() -> new UserNotFoundException(
                            "Worker not found with ID: " + workerId));

            validateWorker(worker);

            WorkerProfile profile = worker.getWorkerProfile();
            if (profile == null || profile.getActiveProjectIds() == null ||
                    profile.getActiveProjectIds().isEmpty()) {
                log.info("Worker has no active projects");
                return List.of();
            }

            String token = getAuthToken();

            List<String> projectIds = profile.getActiveProjectIds();
            List<ProjectResponse> projects = new ArrayList<>();

            for (String projectId : projectIds) {
                try {
                    ProjectResponse project = webClientBuilder.build()
                            .get()
                            .uri("http://localhost:9095/api/projects/{projectId}", projectId)
                            .header("Authorization", "Bearer " + token)
                            .retrieve()
                            .bodyToMono(ProjectResponse.class)
                            .block();

                    if (project != null) {
                        projects.add(project);
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch project {}: {}", projectId, e.getMessage());
                }
            }

            log.info("Found {} projects for worker {}", projects.size(), workerId);
            return projects;

        } catch (UserNotFoundException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching worker projects: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to fetch worker projects", e);
        }
    }

    @Transactional
    public void removeEmployeeFromCompany(String companyId, String userId, String removedBy) {
        try {
            log.info("Removing employee {} from company {}", userId, companyId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

            if (user.getCompanyId() == null || !user.getCompanyId().equals(companyId)) {
                throw new InvalidInputException("User does not belong to this company");
            }

            if (user.getRole() == UserRole.WORKER && user.getWorkerProfile() != null) {
                WorkerProfile workerProfile = user.getWorkerProfile();

                if (workerProfile.getActiveProjectIds() != null && !workerProfile.getActiveProjectIds().isEmpty()) {
                    log.info("Removing worker from {} projects", workerProfile.getActiveProjectIds().size());
                    workerProfile.getActiveProjectIds().clear();
                    workerProfile.setIsAvailable(true);
                }

                // ✅ Unit atamalarını da temizle
                if (workerProfile.getAssignedUnitIds() != null) {
                    workerProfile.getAssignedUnitIds().clear();
                }
            }

            user.setCompanyId(null);
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);

            auditLogService.logUserEvent(
                    AuditEvent.USER_UPDATED,
                    user.getId(),
                    removedBy,
                    "Employee removed from company: " + companyId
            );

            publishWorkerRemovedFromCompanyEvent(companyId, userId, removedBy);

            log.info("✅ Successfully removed employee {} from company {}", userId, companyId);

        } catch (UserNotFoundException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error removing employee from company: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to remove employee from company", e);
        }
    }

    private void publishWorkerRemovedFromCompanyEvent(String companyId, String userId, String removedBy) {
        try {
            WorkerRemovedFromCompanyEvent event = WorkerRemovedFromCompanyEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(java.time.OffsetDateTime.now().toString())
                    .companyId(companyId)
                    .userId(userId)
                    .removedBy(removedBy)
                    .reason("Removed from company")
                    .build();

            kafkaTemplate.send("worker.removed", event);
            log.info("✅ Published worker.removed event: company={}, user={}", companyId, userId);

        } catch (Exception e) {
            log.warn("Failed to publish worker.removed event: {}", e.getMessage());
        }
    }

    private String getAuthToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                log.warn("❌ No authentication in SecurityContext");
                return null;
            }

            if (!(authentication.getPrincipal() instanceof Jwt)) {
                log.warn("❌ Principal is not JWT: {}",
                        authentication.getPrincipal().getClass().getSimpleName());
                return null;
            }

            Jwt jwt = (Jwt) authentication.getPrincipal();
            log.debug("✅ JWT token retrieved");
            return jwt.getTokenValue();

        } catch (Exception e) {
            log.error("❌ Error getting auth token: {}", e.getMessage());
            return null;
        }
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchWorkers(
            WorkerSpecialty specialty,
            String city,
            Boolean isAvailable,
            BigDecimal minRating,
            Pageable pageable) {

        try {
            log.info("Searching workers - Specialty: {}, City: {}, Available: {}, MinRating: {}",
                    specialty, city, isAvailable, minRating);

            validateSearchParameters(minRating);

            Page<User> workers = userRepository.searchWorkers(
                    specialty != null ? specialty.name() : null,
                    city,
                    isAvailable,
                    minRating != null ? minRating : BigDecimal.ZERO,
                    UserRole.WORKER.name(),
                    UserStatus.ACTIVE.name(),
                    pageable
            );

            log.debug("Found {} workers", workers.getTotalElements());

            return workers.map(userMapper::toResponse);

        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Worker search error: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to search workers", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getCompanyWorkers(String companyId, Pageable pageable) {
        try {
            log.info("Fetching company workers - CompanyId: {}", companyId);

            if (companyId == null || companyId.isBlank()) {
                throw new InvalidInputException("Company ID is required");
            }

            Page<User> workers = userRepository.findByCompanyIdAndRoleAndStatus(
                    companyId,
                    UserRole.WORKER,
                    UserStatus.ACTIVE,
                    pageable
            );

            log.debug("Found {} workers for company {}", workers.getTotalElements(), companyId);

            return workers.map(userMapper::toResponse);

        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching company workers: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to fetch company workers", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllCompanyEmployees(String companyId, Pageable pageable) {
        try {
            log.info("Fetching all company employees - CompanyId: {}", companyId);

            if (companyId == null || companyId.isBlank()) {
                throw new InvalidInputException("Company ID is required");
            }

            Page<User> employees = userRepository.findByCompanyIdAndStatus(
                    companyId,
                    UserStatus.ACTIVE,
                    pageable
            );

            log.debug("Found {} employees for company {}", employees.getTotalElements(), companyId);

            return employees.map(userMapper::toResponse);

        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching company employees: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to fetch company employees", e);
        }
    }

    @Transactional
    public UserResponse updateWorkerAvailability(
            UUID workerId,
            boolean isAvailable,
            LocalDate availableFrom,
            String updatedBy) {

        try {
            log.info("Updating worker availability - WorkerId: {}, Available: {}, From: {}",
                    workerId, isAvailable, availableFrom);

            User worker = userRepository.findById(workerId.toString())
                    .orElseThrow(() -> new UserNotFoundException("Worker not found with ID: " + workerId));

            validateWorker(worker);

            worker.setIsAvailable(isAvailable);

            if (worker.getWorkerProfile() != null && availableFrom != null) {
                worker.getWorkerProfile().setAvailableFrom(availableFrom);
            }

            worker = userRepository.save(worker);

            auditLogService.logUserEvent(
                    AuditEvent.USER_UPDATED,
                    worker.getId(),
                    updatedBy,
                    "Worker availability updated: " + (isAvailable ? "Available" : "Not Available")
            );

            log.info("Worker availability updated - WorkerId: {}", workerId);

            return userMapper.toResponse(worker);

        } catch (UserNotFoundException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating worker availability: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to update worker availability", e);
        }
    }

    @Transactional
    public void updateWorkerPerformance(
            UUID workerId,
            boolean onTime,
            BigDecimal taskRating,
            String updatedBy) {

        try {
            log.info("Updating worker performance - WorkerId: {}, OnTime: {}, Rating: {}",
                    workerId, onTime, taskRating);

            validateRating(taskRating);

            User worker = userRepository.findById(workerId.toString())
                    .orElseThrow(() -> new UserNotFoundException("Worker not found with ID: " + workerId));

            validateWorker(worker);

            WorkerProfile profile = worker.getWorkerProfile();
            if (profile == null) {
                throw new InvalidInputException("Worker profile not found");
            }

            Integer completedTasks = profile.getCompletedTasks() != null
                    ? profile.getCompletedTasks() + 1
                    : 1;
            profile.setCompletedTasks(completedTasks);

            updateAverageRating(profile, taskRating, completedTasks);
            updateCompletionCounts(profile, onTime);
            updateReliabilityScore(profile);

            Integer totalWorkDays = profile.getTotalWorkDays() != null
                    ? profile.getTotalWorkDays() + 1
                    : 1;
            profile.setTotalWorkDays(totalWorkDays);

            userRepository.save(worker);

            auditLogService.logUserEvent(
                    AuditEvent.USER_UPDATED,
                    worker.getId(),
                    updatedBy,
                    String.format("Performance updated - Rating: %.2f, OnTime: %s", taskRating, onTime)
            );

            log.info("Worker performance updated - WorkerId: {}, New Rating: {}, Reliability: {}%",
                    workerId, profile.getRating(), profile.getReliabilityScore());

        } catch (UserNotFoundException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating worker performance: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to update worker performance", e);
        }
    }

    @Transactional
    public void addWorkerToProject(UUID workerId, String projectId, String updatedBy) {
        try {
            log.info("Adding worker to project - WorkerId: {}, ProjectId: {}", workerId, projectId);

            User worker = userRepository.findById(workerId.toString())
                    .orElseThrow(() -> new UserNotFoundException("Worker not found with ID: " + workerId));

            validateWorker(worker);

            WorkerProfile profile = worker.getWorkerProfile();
            if (profile == null) {
                throw new InvalidInputException("Worker profile not found");
            }

            if (profile.getActiveProjectIds() == null) {
                profile.setActiveProjectIds(new ArrayList<>());
            }

            if (!profile.getActiveProjectIds().contains(projectId)) {
                profile.getActiveProjectIds().add(projectId);
            }

            worker.setIsAvailable(false);

            String companyId = getCompanyIdFromProject(projectId);
            worker.setCompanyId(companyId);

            userRepository.save(worker);

            auditLogService.logUserEvent(
                    AuditEvent.USER_UPDATED,
                    worker.getId(),
                    updatedBy,
                    "Worker assigned to project: " + projectId
            );

            publishWorkerAssignedEvent(worker, projectId, updatedBy);

            log.info("✅ Worker added to project successfully - WorkerId: {}, ProjectId: {}", workerId, projectId);

        } catch (UserNotFoundException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding worker to project: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to add worker to project", e);
        }
    }

    private String getCompanyIdFromProject(String projectId) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:9095/api/projects/{projectId}/company-id", projectId)
                .header("Authorization", "Bearer " + getAuthToken())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @Transactional
    public void removeWorkerFromProject(UUID workerId, String projectId, String updatedBy) {
        try {
            log.info("Removing worker from project - WorkerId: {}, ProjectId: {}", workerId, projectId);

            User worker = userRepository.findById(workerId.toString())
                    .orElseThrow(() -> new UserNotFoundException("Worker not found with ID: " + workerId));

            validateWorker(worker);

            WorkerProfile profile = worker.getWorkerProfile();
            if (profile != null && profile.getActiveProjectIds() != null) {
                profile.getActiveProjectIds().remove(projectId);

                if (profile.getActiveProjectIds().isEmpty()) {
                    worker.setIsAvailable(true);
                    worker.setCompanyId(null);
                }
            }

            userRepository.save(worker);

            auditLogService.logUserEvent(
                    AuditEvent.USER_UPDATED,
                    worker.getId(),
                    updatedBy,
                    "Worker removed from project: " + projectId
            );

            log.info("✅ Worker removed from project successfully - WorkerId: {}, ProjectId: {}", workerId, projectId);

        } catch (UserNotFoundException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error removing worker from project: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to remove worker from project", e);
        }
    }

    private void publishWorkerAssignedEvent(User worker, String projectId, String issuedBy) {
        try {
            String token = getAuthToken();
            String companyId = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:9095/api/projects/{projectId}/company-id", projectId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (companyId == null) {
                log.warn("Cannot publish worker.assigned event, companyId null for project {}", projectId);
                return;
            }

            WorkerAssignedToProjectEvent evt = WorkerAssignedToProjectEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(java.time.OffsetDateTime.now().toString())
                    .companyId(companyId)
                    .projectId(projectId)
                    .userId(worker.getId())
                    .position("Worker")
                    .department(null)
                    .role("WORKER")
                    .issuedBy(issuedBy)
                    .build();

            kafkaTemplate.send("worker.assigned", evt);
            log.info("✅ Published worker.assigned event: worker={}, company={}", worker.getId(), companyId);

        } catch (Exception e) {
            log.warn("Failed to publish worker.assigned: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getTopWorkersBySpecialty(WorkerSpecialty specialty, int limit) {
        try {
            log.info("Fetching top {} workers by specialty: {}", limit, specialty);

            Pageable pageable = PageRequest.of(0, limit, Sort.by("workerProfile.rating").descending());

            Page<User> topWorkers = userRepository.findTopWorkersBySpecialty(
                    specialty,
                    UserStatus.ACTIVE,
                    pageable
            );

            return topWorkers.map(userMapper::toResponse).getContent();

        } catch (Exception e) {
            log.error("Error fetching top workers: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to fetch top workers", e);
        }
    }

    @Transactional(readOnly = true)
    public WorkerStatsResponse getWorkerStats(UUID workerId) {
        try {
            log.debug("Fetching worker stats - WorkerId: {}", workerId);

            User worker = userRepository.findById(workerId.toString())
                    .orElseThrow(() -> new UserNotFoundException("Worker not found with ID: " + workerId));

            validateWorker(worker);

            WorkerProfile profile = worker.getWorkerProfile();
            if (profile == null) {
                throw new InvalidInputException("Worker profile not found");
            }

            return WorkerStatsResponse.builder()
                    .workerId(worker.getId())
                    .fullName(worker.getFirstName() + " " + worker.getLastName())
                    .specialty(profile.getSpecialty())
                    .rating(profile.getRating())
                    .completedTasks(profile.getCompletedTasks())
                    .totalWorkDays(profile.getTotalWorkDays())
                    .onTimeCompletionCount(profile.getOnTimeCompletionCount())
                    .lateCompletionCount(profile.getLateCompletionCount())
                    .reliabilityScore(profile.getReliabilityScore())
                    .activeProjectCount(profile.getActiveProjectIds() != null
                            ? profile.getActiveProjectIds().size()
                            : 0)
                    .isAvailable(worker.getIsAvailable())
                    .experienceYears(profile.getExperienceYears())
                    .build();

        } catch (UserNotFoundException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching worker stats: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to fetch worker stats", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAvailableWorkers(
            String city,
            WorkerSpecialty specialty,
            Pageable pageable) {

        try {
            log.info("Fetching available workers - City: {}, Specialty: {}", city, specialty);

            Page<User> workers = userRepository.findAvailableWorkers(
                    city,
                    specialty,
                    UserStatus.ACTIVE,
                    pageable
            );

            return workers.map(userMapper::toResponse);

        } catch (Exception e) {
            log.error("Error fetching available workers: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to fetch available workers", e);
        }
    }

    private void validateWorker(User worker) {
        if (worker.getRole() != UserRole.WORKER) {
            throw new InvalidInputException("User is not a worker");
        }

        if (worker.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidInputException("Worker is not active");
        }
    }

    private void updateAverageRating(WorkerProfile profile, BigDecimal newRating, int totalTasks) {
        BigDecimal currentRating = profile.getRating();
        BigDecimal updatedRating;

        if (currentRating == null || currentRating.compareTo(BigDecimal.ZERO) == 0) {
            updatedRating = newRating;
        } else {
            BigDecimal totalScore = currentRating
                    .multiply(BigDecimal.valueOf(totalTasks - 1))
                    .add(newRating);
            updatedRating = totalScore.divide(
                    BigDecimal.valueOf(totalTasks),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        profile.setRating(updatedRating);
    }

    private void updateCompletionCounts(WorkerProfile profile, boolean onTime) {
        if (onTime) {
            Integer count = profile.getOnTimeCompletionCount() != null
                    ? profile.getOnTimeCompletionCount() + 1
                    : 1;
            profile.setOnTimeCompletionCount(count);
        } else {
            Integer count = profile.getLateCompletionCount() != null
                    ? profile.getLateCompletionCount() + 1
                    : 1;
            profile.setLateCompletionCount(count);
        }
    }

    private void updateReliabilityScore(WorkerProfile profile) {
        Integer onTimeCount = profile.getOnTimeCompletionCount() != null
                ? profile.getOnTimeCompletionCount()
                : 0;
        Integer lateCount = profile.getLateCompletionCount() != null
                ? profile.getLateCompletionCount()
                : 0;

        int totalTasks = onTimeCount + lateCount;

        if (totalTasks > 0) {
            BigDecimal reliabilityScore = BigDecimal.valueOf(onTimeCount)
                    .divide(BigDecimal.valueOf(totalTasks), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            profile.setReliabilityScore(reliabilityScore);
        } else {
            profile.setReliabilityScore(BigDecimal.valueOf(100));
        }
    }

    private void validateSearchParameters(BigDecimal minRating) {
        if (minRating != null) {
            if (minRating.compareTo(BigDecimal.ZERO) < 0 || minRating.compareTo(BigDecimal.valueOf(5)) > 0) {
                throw new InvalidInputException("Rating must be between 0 and 5");
            }
        }
    }

    private void validateRating(BigDecimal rating) {
        if (rating == null) {
            throw new InvalidInputException("Rating is required");
        }
        if (rating.compareTo(BigDecimal.ONE) < 0 || rating.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new InvalidInputException("Rating must be between 1 and 5");
        }
    }
}