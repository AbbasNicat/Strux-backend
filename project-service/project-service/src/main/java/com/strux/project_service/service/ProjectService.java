package com.strux.project_service.service;

import com.strux.project_service.client.TaskClient;
import com.strux.project_service.client.UnitClient;
import com.strux.project_service.client.WorkerClient;
import com.strux.project_service.config.SecurityUtils;
import com.strux.project_service.dto.*;
import com.strux.project_service.enums.PhaseStatus;
import com.strux.project_service.enums.ProjectStatus;
import com.strux.project_service.enums.ProjectType;
import com.strux.project_service.event.*;
import com.strux.project_service.exceptions.InvalidPhaseException;
import com.strux.project_service.mapper.ProjectMapper;
import com.strux.project_service.model.Project;
import com.strux.project_service.model.ProjectPhase;
import com.strux.project_service.repository.ProjectPhaseRepository;
import com.strux.project_service.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ProjectMapper projectMapper;
    private final RestTemplate restTemplate;
    private final ProjectPhaseRepository projectPhaseRepository;
    private final SecurityUtils securityUtils; // Constructor'a eklenecek

    private final WorkerClient workerClient;
    private final UnitClient unitClient;
    private final TaskClient taskClient;

    public List<ProjectStatsResponse> getCompanyProjectStats(String companyId) {
        log.info("📊 Getting project stats for company: {}", companyId);

        List<Project> projects = projectRepository.findByCompanyId(companyId);

        return projects.stream()
                .map(project -> {

                    Long workerCount = 0L;
                    Long unitCount = 0L;
                    Long taskCount = 0L;

                    // ✅ Worker count with proper error logging
                    try {
                        workerCount = workerClient.getProjectWorkerStats(project.getId()).getTotalWorkers();
                        log.info("✅ Project {} has {} workers", project.getId(), workerCount);
                    } catch (Exception e) {
                        log.error("❌ Failed to get worker count for project {}: {}",
                                project.getId(), e.getMessage(), e);
                    }

                    // ✅ Unit count with proper error logging
                    try {
                        unitCount = unitClient.countUnitsByProject(project.getId());
                        log.info("✅ Project {} has {} units", project.getId(), unitCount);
                    } catch (Exception e) {
                        log.error("❌ Failed to get unit count for project {}: {}",
                                project.getId(), e.getMessage(), e);
                    }

                    // ✅ Task count with proper error logging
                    try {
                        taskCount = taskClient.getProjectTaskStats(project.getId()).getTotal();
                        log.info("✅ Project {} has {} tasks", project.getId(), taskCount);
                    } catch (Exception e) {
                        log.error("❌ Failed to get task count for project {}: {}",
                                project.getId(), e.getMessage(), e);
                    }

                    return new ProjectStatsResponse(
                            project.getId(),
                            project.getName(),
                            workerCount,
                            unitCount,
                            taskCount,
                            project.getCompletionPercentage()
                    );
                })
                .toList();
    }

    private String getCompanyIdFromToken() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwt = (Jwt) auth.getPrincipal();

            // 1. Önce token'da company_id var mı bak
            String companyId = jwt.getClaim("company_id");
            if (companyId != null) {
                return companyId;
            }

            // 2. Yoksa email al
            String email = jwt.getClaim("email");
            log.info("Getting company ID for email: {}", email);

            // 3. User Service'e GET request at
            String url = "http://localhost:9091/api/users/email/" + email;
            UserResponse user = restTemplate.getForObject(url, UserResponse.class);

            return user.getCompanyId();

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException("Error fetching company ID");
        }
    }
    @lombok.Data
    private static class UserResponse {
        private String companyId;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        try {
            String companyId = securityUtils.getCurrentUserCompanyId();
            validatPhaseWeights(request.getPhases());

            Project project = new Project();
            project.setName(request.getName());
            project.setDescription(request.getDescription());
            project.setType(request.getType());
            project.setCompanyId(companyId);
            project.setStatus(ProjectStatus.PLANNING);

            Project.ProjectLocation location = new Project.ProjectLocation();
            location.setLatitude(request.getLatitude());
            location.setLongitude(request.getLongitude());
            location.setAddress(request.getAddress());
            location.setCity(request.getCity());
            location.setDistrict(request.getDistrict());
            location.setCountry(request.getCountry());
            location.setPlaceId(request.getPlaceId());
            project.setLocation(location);

            project.setStartDate(request.getStartDate());
            project.setPlannedEndDate(request.getPlannedEndDate());
            project.setTotalUnits(request.getTotalUnits());
            project.setImageUrl(request.getImageUrl());
            project.setOverallProgress(BigDecimal.ZERO);

            LocalDateTime now = LocalDateTime.now();
            project.setCreatedAt(now);
            project.setUpdatedAt(now);

            Project savedProject = projectRepository.save(project);

            if (request.getPhases() != null && !request.getPhases().isEmpty()) {
                Project finalSavedProject = savedProject;
                List<ProjectPhase> phases = request.getPhases().stream()
                        .map(phaseReq -> createPhase(phaseReq, finalSavedProject))
                        .collect(Collectors.toList());

                savedProject.setPhases(phases);
                savedProject = projectRepository.save(savedProject);
            }

            try {
                if (kafkaTemplate != null) {
                    ProjectCreatedEvent event = ProjectCreatedEvent.builder()
                            .projectId(savedProject.getId())
                            .companyId(savedProject.getCompanyId())
                            .name(savedProject.getName())
                            .description(savedProject.getDescription())
                            .type(savedProject.getType())
                            .totalUnits(savedProject.getTotalUnits())
                            .location(ProjectCreatedEvent.LocationInfo.builder()
                                    .latitude(savedProject.getLocation().getLatitude())
                                    .longitude(savedProject.getLocation().getLongitude())
                                    .address(savedProject.getLocation().getAddress())
                                    .city(savedProject.getLocation().getCity())
                                    .district(savedProject.getLocation().getDistrict())
                                    .country(savedProject.getLocation().getCountry())
                                    .placeId(savedProject.getLocation().getPlaceId())
                                    .build())
                            .startDate(savedProject.getStartDate())
                            .plannedEndDate(savedProject.getPlannedEndDate())
                            .timestamp(LocalDateTime.now())
                            .build();

                    kafkaTemplate.send("project.created", event);
                    log.info("✅ Project created event published successfully");
                } else {
                    log.warn("⚠️ KafkaTemplate is null, skipping event publish");
                }
            } catch (Exception kafkaEx) {
                // Kafka hatası olsa bile proje oluşturulmuş olsun
                log.error("❌ Failed to publish Kafka event but project created successfully: {}",
                        kafkaEx.getMessage(), kafkaEx);
            }

            return projectMapper.toProjectResponse(savedProject);

        } catch (InvalidPhaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating project", e);
            throw new RuntimeException("Error creating project: " + e.getMessage(), e);
        }
    }

    // hard delete
    @Transactional
    public void deleteProject(String projectId) {
        try {
            String companyId = securityUtils.getCurrentUserCompanyId();
            Project project = projectRepository.findByIdAndCompanyId(projectId, companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found or access denied"));

            String projectName = project.getName();
            String projectCompanyId = project.getCompanyId();

            projectRepository.delete(project);
            projectRepository.flush(); // Ensure deletion is committed

            log.info("✅ Project {} permanently deleted from database", projectId);

            try {
                ProjectDeletedEvent event = ProjectDeletedEvent.builder()
                        .projectId(projectId)
                        .companyId(projectCompanyId)
                        .projectName(projectName)
                        .reason("Project permanently deleted by user")
                        .timestamp(LocalDateTime.now())
                        .build();

                kafkaTemplate.send("project.deleted", event);
                log.info("✅ Project deletion event published successfully");
            } catch (Exception kafkaEx) {
                // Kafka hatası olsa bile proje silinmiş olsun
                log.error("❌ Failed to publish deletion event but project deleted: {}",
                        kafkaEx.getMessage(), kafkaEx);
            }

        } catch (EntityNotFoundException e) {
            log.error("Project not found: {}", projectId);
            throw e;
        } catch (Exception e) {
            log.error("Error deleting project {}: {}", projectId, e.getMessage(), e);
            throw new RuntimeException("Error deleting project: " + e.getMessage(), e);
        }
    }

    public ProjectResponse updateProject(UpdateProjectRequest request, String projectId) {
        try {

            String companyId = securityUtils.getCurrentUserCompanyId();
            Project project = projectRepository.findByIdAndCompanyId(projectId, companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found or access denied"));
            List<String> changedFields = new ArrayList<>();

            // Track location changes
            Project.ProjectLocation oldLocation = null;
            boolean locationChanged = false;
            if (request.getLatitude() != null || request.getLongitude() != null ||
                    request.getCity() != null || request.getDistrict() != null) {
                oldLocation = new Project.ProjectLocation(
                        project.getLocation().getLatitude(),
                        project.getLocation().getLongitude(),
                        project.getLocation().getAddress(),
                        project.getLocation().getCity(),
                        project.getLocation().getDistrict(),
                        project.getLocation().getCountry(),
                        project.getLocation().getPlaceId()
                );
                locationChanged = true;
                changedFields.add("location");
            }

            // Track schedule changes
            LocalDate oldPlannedEndDate = project.getPlannedEndDate();
            if (request.getPlannedEndDate() != null && !request.getPlannedEndDate().equals(oldPlannedEndDate)) {
                changedFields.add("plannedEndDate");
            }

            // Track units changes
            Integer oldTotalUnits = project.getTotalUnits();
            if (request.getTotalUnits() != null && !request.getTotalUnits().equals(oldTotalUnits)) {
                changedFields.add("totalUnits");
            }

            // Track image changes
            if (request.getImageUrl() != null && !request.getImageUrl().equals(project.getImageUrl())) {
                changedFields.add("imageUrl");
            }

            if (request.getName() != null) changedFields.add("name");
            if (request.getDescription() != null) changedFields.add("description");

            updateProjectFields(request, project);
            project.setUpdatedAt(LocalDateTime.now());

            Project updatedProject = projectRepository.save(project);

            // ✅ PROJECT UPDATED EVENT
            if (!changedFields.isEmpty()) {
                ProjectUpdatedEvent event = ProjectUpdatedEvent.builder()
                        .projectId(updatedProject.getId())
                        .companyId(updatedProject.getCompanyId())
                        .changedFields(changedFields)
                        .timestamp(LocalDateTime.now())
                        .build();

                kafkaTemplate.send("project.updated", event);
            }

            // ✅ PROJECT SCHEDULE UPDATED EVENT
            if (request.getPlannedEndDate() != null && !request.getPlannedEndDate().equals(oldPlannedEndDate)) {
                long daysDifference = ChronoUnit.DAYS.between(oldPlannedEndDate, request.getPlannedEndDate());

                ProjectScheduleUpdatedEvent scheduleEvent = ProjectScheduleUpdatedEvent.builder()
                        .projectId(updatedProject.getId())
                        .companyId(updatedProject.getCompanyId())
                        .oldPlannedEndDate(oldPlannedEndDate)
                        .newPlannedEndDate(request.getPlannedEndDate())
                        .daysDifference((int) daysDifference)
                        .reason("Schedule updated")
                        .timestamp(LocalDateTime.now())
                        .build();

                kafkaTemplate.send("project.schedule.updated", scheduleEvent);
            }

            // ✅ PROJECT UNITS UPDATED EVENT
            if (request.getTotalUnits() != null && !request.getTotalUnits().equals(oldTotalUnits)) {
                ProjectUnitsUpdatedEvent unitsEvent = ProjectUnitsUpdatedEvent.builder()
                        .projectId(updatedProject.getId())
                        .companyId(updatedProject.getCompanyId())
                        .previousTotalUnits(oldTotalUnits)
                        .newTotalUnits(request.getTotalUnits())
                        .unitsAdded(request.getTotalUnits() - oldTotalUnits)
                        .timestamp(LocalDateTime.now())
                        .build();

                kafkaTemplate.send("project.units.updated", unitsEvent);
            }

            return projectMapper.toProjectResponse(updatedProject);

        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Project not found with id: " + projectId);
        }
    }

    private ProjectPhase createPhase(CreateProjectRequest.PhaseDefinition phaseReq, Project project) {

        ProjectPhase phase = new ProjectPhase();
        phase.setProject(project);
        phase.setPhaseName(phaseReq.getPhaseName());
        phase.setDescription(phaseReq.getDescription());
        phase.setWeightPercentage(phaseReq.getWeightPercentage());
        phase.setCurrentProgress(BigDecimal.ZERO);
        phase.setStatus(PhaseStatus.NOT_STARTED);
        phase.setPlannedStartDate(phaseReq.getPlannedStartDate());
        phase.setPlannedEndDate(phaseReq.getPlannedEndDate());
        phase.setOrderIndex(phaseReq.getOrderIndex());

        LocalDateTime now = LocalDateTime.now();
        phase.setCreatedAt(now);
        phase.setUpdatedAt(now);

        return phase;
    }

    private void validatPhaseWeights(List<CreateProjectRequest.PhaseDefinition> phases) {
        if (phases == null || phases.isEmpty()) {
            throw new InvalidPhaseException("At least 1 phase is required");
        }

        BigDecimal totalWeight = phases.stream()
                .map(CreateProjectRequest.PhaseDefinition::getWeightPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalWeight.compareTo(new BigDecimal("100")) != 0) {
            throw new InvalidPhaseException(
                    "Total phase must be 100%. Now " + totalWeight + "%"
            );
        }
    }

    public List<ProjectResponse> getProjectsByCompanyId(String requestedCompanyId) {
        try {
            // ✅ Güvenlik: Kullanıcı sadece kendi company'sini görebilir
            String currentUserCompanyId = securityUtils.getCurrentUserCompanyId();

            if (!currentUserCompanyId.equals(requestedCompanyId)) {
                log.warn("User from company {} tried to access projects from company {}",
                        currentUserCompanyId, requestedCompanyId);
                throw new SecurityException("Cannot access projects from other companies");
            }

            List<Project> projects = projectRepository.findByCompanyId(requestedCompanyId);
            return projectMapper.toProjectResponseList(projects);

        } catch (EntityNotFoundException e) {
            throw e;
        }
    }

    @Transactional
    public ProjectDetailResponse getProjectDetailById(String projectId, String companyId) {
        try {
            log.info("Fetching project detail: {}", projectId);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // Rolleri kontrol et
            boolean isWorker = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_WORKER"));

            boolean isHomeowner = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_HOMEOWNER"));

            Project project;

            // ✅ WORKER veya HOMEOWNER için company kontrolü yok
            if (isWorker || isHomeowner) {
                log.info("🔓 {} role detected - skipping company check",
                        isWorker ? "WORKER" : "HOMEOWNER");

                project = projectRepository.findById(projectId)
                        .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));
            }
            // ✅ Diğer roller için company kontrolü yap
            else {
                String effectiveCompanyId = (companyId != null && !companyId.isBlank())
                        ? companyId
                        : securityUtils.getCurrentUserCompanyId();

                log.info("🔒 Using companyId for security check: {}", effectiveCompanyId);

                project = projectRepository.findByIdAndCompanyId(projectId, effectiveCompanyId)
                        .orElseThrow(() -> new EntityNotFoundException("Project not found or access denied"));
            }

            return projectMapper.toProjectDetailResponse(project);

        } catch (EntityNotFoundException e) {
            log.error("Project not found: {}", projectId);
            throw e;
        } catch (Exception e) {
            log.error("Error fetching project detail: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch project detail", e);
        }
    }

    public List<ProjectResponse> getAllProjects() {
        try {
            String companyId = securityUtils.getCurrentUserCompanyId();
            log.info("Fetching all projects for company: {}", companyId);

            List<Project> projects = projectRepository.findByCompanyId(companyId);
            return projectMapper.toProjectResponseList(projects);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching all projects");
        }
    }

    public String getCompanyIdByProjectId(String projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"))
                .getCompanyId();
    }


    @Transactional
    public ProjectMapResponse partialUpdateProject(String projectId, Map<String, Object> updates) {
        log.info("Partial update for project {} with fields: {}", projectId, updates.keySet());

        String companyId = securityUtils.getCurrentUserCompanyId();
        Project project = projectRepository.findByIdAndCompanyId(projectId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found or access denied"));
        updates.forEach((key, value) -> {
            if (value != null) {
                try {
                    // ✅ Eğer "location" object'i gönderilmişse
                    if (key.equals("location") && value instanceof Map) {
                        updateLocationFromMap(project, (Map<String, Object>) value);
                    }
                    // ✅ Eğer "basicInfo" object'i gönderilmişse
                    else if (key.equals("basicInfo") && value instanceof Map) {
                        updateBasicInfoFromMap(project, (Map<String, Object>) value);
                    }
                    // Normal field update
                    else {
                        updateFieldDynamically(project, key, value);
                    }
                } catch (Exception e) {
                    log.error("Could not update field {}: {}", key, e.getMessage());
                }
            }
        });

        project.setUpdatedAt(LocalDateTime.now());
        Project saved = projectRepository.save(project);

        log.info("Project {} partially updated successfully", projectId);
        return projectMapper.toProjectMapResponse(saved);
    }

    private void updateFieldDynamically(Project project, String fieldName, Object value) {
        try {
            Field field = Project.class.getDeclaredField(fieldName);
            field.setAccessible(true);

            // Type conversion
            Object convertedValue = convertValue(value, field.getType());
            field.set(project, convertedValue);

        } catch (NoSuchFieldException e) {
            log.warn("Field {} not found in Project entity", fieldName);
            throw new IllegalArgumentException("Invalid field: " + fieldName);
        } catch (IllegalAccessException e) {
            log.warn("Field {} not accessible in Project entity", fieldName);
            throw new IllegalArgumentException("Cannot access field: " + fieldName);
        } catch (Exception e) {
            log.error("Error updating field {}: {}", fieldName, e.getMessage());
            throw new IllegalArgumentException("Error updating field " + fieldName + ": " + e.getMessage());
        }
    }

    private void updateLocationFromMap(Project project, Map<String, Object> locationMap) {
        if (project.getLocation() == null) {
            project.setLocation(new Project.ProjectLocation());
        }

        Project.ProjectLocation location = project.getLocation();

        if (locationMap.containsKey("latitude")) {
            Object lat = locationMap.get("latitude");
            location.setLatitude(lat != null ? Double.parseDouble(String.valueOf(lat)) : null);
        }
        if (locationMap.containsKey("longitude")) {
            Object lng = locationMap.get("longitude");
            location.setLongitude(lng != null ? Double.parseDouble(String.valueOf(lng)) : null);
        }
        if (locationMap.containsKey("address")) {
            location.setAddress((String) locationMap.get("address"));
        }
        if (locationMap.containsKey("city")) {
            location.setCity((String) locationMap.get("city"));
        }
        if (locationMap.containsKey("district")) {
            location.setDistrict((String) locationMap.get("district"));
        }
        if (locationMap.containsKey("country")) {
            location.setCountry((String) locationMap.get("country"));
        }
        if (locationMap.containsKey("placeId")) {
            location.setPlaceId((String) locationMap.get("placeId"));
        }
    }

    private void updateBasicInfoFromMap(Project project, Map<String, Object> basicInfoMap) {
        if (basicInfoMap.containsKey("name")) {
            project.setName((String) basicInfoMap.get("name"));
        }
        if (basicInfoMap.containsKey("description")) {
            project.setDescription((String) basicInfoMap.get("description"));
        }
        if (basicInfoMap.containsKey("totalBudget")) {
            Object budget = basicInfoMap.get("totalBudget");
            project.setTotalBudget(budget != null ? Double.parseDouble(String.valueOf(budget)) : null);
        }
        if (basicInfoMap.containsKey("spentBudget")) {
            Object spent = basicInfoMap.get("spentBudget");
            project.setSpentBudget(spent != null ? Double.parseDouble(String.valueOf(spent)) : null);
        }
        if (basicInfoMap.containsKey("completionPercentage")) {
            Object progress = basicInfoMap.get("completionPercentage");
            project.setOverallProgress(progress != null ? BigDecimal.valueOf(Double.parseDouble(String.valueOf(progress))) : null);
        }
        if (basicInfoMap.containsKey("type")) {
            project.setType(ProjectType.valueOf((String) basicInfoMap.get("type")));
        }
        if (basicInfoMap.containsKey("status")) {
            project.setStatus(ProjectStatus.valueOf((String) basicInfoMap.get("status")));
        }
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType == BigDecimal.class) {
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            }
            if (value instanceof String) {
                return new BigDecimal((String) value);
            }
        }

        if (targetType == Double.class || targetType == double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            if (value instanceof String) {
                return Double.parseDouble((String) value);
            }
        }

        if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            if (value instanceof String) {
                return Integer.parseInt((String) value);
            }
        }

        if (targetType == ProjectType.class && value instanceof String) {
            return ProjectType.valueOf((String) value);
        }

        if (targetType == ProjectStatus.class && value instanceof String) {
            return ProjectStatus.valueOf((String) value);
        }

        if (targetType == LocalDateTime.class && value instanceof String) {
            return LocalDateTime.parse((String) value);
        }

        if (targetType == LocalDate.class && value instanceof String) {
            return LocalDate.parse((String) value);
        }

        return value;
    }

    private void updateBasicInfo(Project project, ProjectBasicInfoDTO basicInfo) {
        if (basicInfo.getName() != null) project.setName(basicInfo.getName());
        if (basicInfo.getDescription() != null) project.setDescription(basicInfo.getDescription());
        if (basicInfo.getType() != null) project.setType(ProjectType.valueOf(basicInfo.getType()));
        if (basicInfo.getStatus() != null) project.setStatus(ProjectStatus.valueOf(basicInfo.getStatus()));
        if (basicInfo.getStartDate() != null) project.setStartDate(LocalDate.from(basicInfo.getStartDate()));
        if (basicInfo.getEndDate() != null) project.setActualEndDate(LocalDate.from(basicInfo.getEndDate()));
        if (basicInfo.getEstimatedEndDate() != null) project.setEstimatedEndDate(LocalDate.from(basicInfo.getEstimatedEndDate()));
        if (basicInfo.getActualEndDate() != null) project.setActualEndDate(LocalDate.from(basicInfo.getActualEndDate()));
        if (basicInfo.getTotalBudget() != null) project.setTotalBudget(basicInfo.getTotalBudget());
        if (basicInfo.getSpentBudget() != null) project.setSpentBudget(basicInfo.getSpentBudget());
        if (basicInfo.getCompletionPercentage() != null) {
            project.setOverallProgress(BigDecimal.valueOf(basicInfo.getCompletionPercentage()));
        }
    }

    private void updateLocationInfo(Project project, LocationInfoDTO locationInfo) {

        if (project.getLocation() == null) {
            project.setLocation(new Project.ProjectLocation());
        }

        Project.ProjectLocation location = project.getLocation();

        if (locationInfo.getLatitude() != null) {
            location.setLatitude(Double.parseDouble(locationInfo.getLatitude()));
        }
        if (locationInfo.getLongitude() != null) {
            location.setLongitude(Double.parseDouble(locationInfo.getLongitude()));
        }
        if (locationInfo.getAddress() != null) {
            location.setAddress(locationInfo.getAddress());
        }
        if (locationInfo.getCity() != null) {
            location.setCity(locationInfo.getCity());
        }
        if (locationInfo.getDistrict() != null) {
            location.setDistrict(locationInfo.getDistrict());
        }
        if (locationInfo.getCountry() != null) {
            location.setCountry(locationInfo.getCountry());
        }
        if (locationInfo.getPlaceId() != null) {
            location.setPlaceId(locationInfo.getPlaceId());
        }
    }
    public List<ProjectMapResponse> getProjectsForMap() {
        try {
            String companyId = securityUtils.getCurrentUserCompanyId();
            List<Project> projects = projectRepository.findByCompanyIdWithLocation(companyId);

            return projects.stream()
                    .map(project -> {
                        ProjectMapResponse response = projectMapper.toProjectMapResponse(project);

                        // ✅ Unit Service-dən unit məlumatlarını çək
                        try {
                            List<UnitMapInfo> units = unitClient.getProjectUnitsForMap(project.getId());
                            response.setUnits(units);
                            log.info("✅ Loaded {} units for project {}", units.size(), project.getId());
                        } catch (Exception e) {
                            log.error("❌ Failed to load units for project {}: {}", project.getId(), e.getMessage());
                            response.setUnits(new ArrayList<>());
                        }

                        return response;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching projects for map", e);
            throw new RuntimeException("Error fetching projects for map", e);
        }
    }

    public ProjectProgressResponse getProjectProgress(String projectId) {
        try {
            // ✅ Company kontrolü eklendi
            String companyId = securityUtils.getCurrentUserCompanyId();

            Project project = projectRepository.findByIdAndCompanyId(projectId, companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found or access denied"));

            return projectMapper.toProjectProgressResponse(project);

        } catch (EntityNotFoundException e) {
            throw e;
        }
    }

    @Transactional
    public void addPhase(String projectId, AddPhaseRequest request) {
        try {
            String companyId = securityUtils.getCurrentUserCompanyId();
            Project project = projectRepository.findByIdAndCompanyId(projectId, companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found or access denied"));
            /*List<CreateProjectRequest.PhaseDefinition> allPhases = project.getPhases().stream()
                    .map(phase -> {
                        CreateProjectRequest.PhaseDefinition def = new CreateProjectRequest.PhaseDefinition();
                        def.setWeightPercentage(phase.getWeightPercentage());
                        return def;
                    })
                    .collect(Collectors.toList());

            CreateProjectRequest.PhaseDefinition newPhaseDef = new CreateProjectRequest.PhaseDefinition();
            newPhaseDef.setWeightPercentage(request.getWeightPercentage());
            allPhases.add(newPhaseDef);

            validatPhaseWeights(allPhases);
            */
            ProjectPhase phase = new ProjectPhase();
            phase.setProject(project);
            phase.setPhaseName(request.getPhaseName());
            phase.setDescription(request.getDescription());
            phase.setWeightPercentage(request.getWeightPercentage());
            phase.setCurrentProgress(BigDecimal.ZERO);
            phase.setStatus(PhaseStatus.NOT_STARTED);
            phase.setPlannedStartDate(request.getPlannedStartDate());
            phase.setPlannedEndDate(request.getPlannedEndDate());
            phase.setOrderIndex(request.getOrderIndex());

            LocalDateTime now = LocalDateTime.now();
            phase.setCreatedAt(now);
            phase.setUpdatedAt(now);

            projectPhaseRepository.save(phase);

        } catch (InvalidPhaseException e) {
            throw e;
        }
    }

    @Transactional
    public void updatePhaseProgress(String projectId, String phaseId, UpdateProgressRequest request) {
        try {
            String companyId = securityUtils.getCurrentUserCompanyId();
            Project project = projectRepository.findByIdAndCompanyId(projectId, companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found or access denied"));
            ProjectPhase phase = projectPhaseRepository.findById(Long.valueOf(phaseId))
                    .orElseThrow(() -> new EntityNotFoundException("Phase not found with id: " + phaseId));

            if (!phase.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("Phase does not belong to this project");
            }

            BigDecimal previousProgress = project.getOverallProgress();

            phase.setCurrentProgress(request.getCurrentProgress());

            if (request.getWeightPercentage() != null) {
                phase.setWeightPercentage(request.getWeightPercentage());
            }

            if (request.getStatus() != null) {
                phase.setStatus(request.getStatus());
            }

            if (request.getActualStartDate() != null) {
                phase.setActualStartDate(request.getActualStartDate());
            }

            if (request.getActualEndDate() != null) {
                phase.setActualEndDate(request.getActualEndDate());

                if (request.getStatus() == PhaseStatus.COMPLETED) {
                    long daysAheadOrBehind = 0;
                    if (phase.getPlannedEndDate() != null) {
                        daysAheadOrBehind = ChronoUnit.DAYS.between(phase.getPlannedEndDate(), phase.getActualEndDate());
                    }

                    ProjectPhaseCompletedEvent phaseEvent = ProjectPhaseCompletedEvent.builder()
                            .projectId(project.getId())
                            .companyId(project.getCompanyId())
                            .phaseId(phase.getId().toString())
                            .phaseName(phase.getPhaseName())
                            .phaseDescription(phase.getDescription())
                            .plannedEndDate(phase.getPlannedEndDate())
                            .completionDate(phase.getActualEndDate())
                            .daysAheadOrBehind((int) daysAheadOrBehind)
                            .phaseProgress(phase.getCurrentProgress())
                            .timestamp(LocalDateTime.now())
                            .build();

                    kafkaTemplate.send("project.phase.completed", phaseEvent);
                }
            }

            phase.setUpdatedAt(LocalDateTime.now());
            projectPhaseRepository.save(phase);

            calculateOverallProgress(project);

            ProjectProgressUpdatedEvent progressEvent = ProjectProgressUpdatedEvent.builder()
                    .projectId(project.getId())
                    .companyId(project.getCompanyId())
                    .previousProgress(previousProgress)
                    .currentProgress(project.getOverallProgress())
                    .progressChange(project.getOverallProgress().subtract(previousProgress))
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("project.progress.updated", progressEvent);

            checkMilestones(project);

            if (project.getOverallProgress().compareTo(new BigDecimal("100")) == 0 &&
                    project.getStatus() != ProjectStatus.COMPLETED) {

                project.setStatus(ProjectStatus.COMPLETED);
                project.setActualEndDate(LocalDate.now());
                projectRepository.save(project);

                // ✅ PROJECT COMPLETED EVENT
                ProjectCompletedEvent completedEvent = ProjectCompletedEvent.builder()
                        .projectId(project.getId())
                        .companyId(project.getCompanyId())
                        .projectName(project.getName())
                        .startDate(project.getStartDate())
                        .plannedEndDate(project.getPlannedEndDate())
                        .completionDate(LocalDate.now())
                        .daysAheadOrBehind((int) ChronoUnit.DAYS.between(project.getPlannedEndDate(), LocalDate.now()))
                        .totalUnits(project.getTotalUnits())
                        .timestamp(LocalDateTime.now())
                        .build();

                kafkaTemplate.send("project.completed", completedEvent);
            }

            // Check delays
            checkDelays(project);

        } catch (EntityNotFoundException e) {
            throw e;
        }
    }

    @Transactional
    public void deletePhase(String projectId, String phaseId) {
        try {
            String companyId = securityUtils.getCurrentUserCompanyId();
            Project project = projectRepository.findByIdAndCompanyId(projectId, companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found or access denied"));
            ProjectPhase phase = projectPhaseRepository.findById(Long.valueOf(phaseId))
                    .orElseThrow(() -> new EntityNotFoundException("Phase not found with id: " + phaseId));

            if (!phase.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("Phase does not belong to this project");
            }

            projectPhaseRepository.delete(phase);
            projectPhaseRepository.flush(); // ✅ Flush

            project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found"));

            calculateOverallProgress(project);

        } catch (EntityNotFoundException e) {
            throw e;
        }
    }

    private void updateProjectFields(UpdateProjectRequest updateProjectRequest, Project project) {
        Project.ProjectLocation location = project.getLocation();
        if (location == null) {
            location = new Project.ProjectLocation();
            project.setLocation(location);
        }

        if (updateProjectRequest.getStatus() != null) {
            project.setStatus(updateProjectRequest.getStatus());
        }
        if (updateProjectRequest.getType() != null) {
            project.setType(updateProjectRequest.getType());
        }

        if (updateProjectRequest.getName() != null) {
            project.setName(updateProjectRequest.getName());
        }
        if (updateProjectRequest.getDescription() != null) {
            project.setDescription(updateProjectRequest.getDescription());
        }
        if (updateProjectRequest.getCity() != null) {
            location.setCity(updateProjectRequest.getCity());
        }
        if (updateProjectRequest.getStartDate() != null) {
            project.setStartDate(updateProjectRequest.getStartDate());
        }
        if (updateProjectRequest.getPlannedEndDate() != null) {
            project.setPlannedEndDate(updateProjectRequest.getPlannedEndDate());
        }
        if (updateProjectRequest.getTotalUnits() != null) {
            project.setTotalUnits(updateProjectRequest.getTotalUnits());
        }
        if (updateProjectRequest.getImageUrl() != null) {
            project.setImageUrl(updateProjectRequest.getImageUrl());
        }
        if (updateProjectRequest.getDistrict() != null) {
            location.setDistrict(updateProjectRequest.getDistrict());
        }
        if (updateProjectRequest.getLatitude() != null) {
            location.setLatitude(updateProjectRequest.getLatitude());
        }
        if (updateProjectRequest.getLongitude() != null) {
            location.setLongitude(updateProjectRequest.getLongitude());
        }
    }

    private void calculateOverallProgress(Project project) {
        List<ProjectPhase> phases = project.getPhases();

        if (phases == null || phases.isEmpty()) {
            project.setOverallProgress(BigDecimal.ZERO);
            return;
        }

        BigDecimal totalProgress = phases.stream()
                .map(phase -> {

                    BigDecimal weight = phase.getWeightPercentage();
                    BigDecimal progress = phase.getCurrentProgress();

                    if (weight != null && progress != null) {
                        return weight.multiply(progress).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    }

                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        project.setOverallProgress(totalProgress);
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    private void checkMilestones(Project project) {
        int[] milestones = {25, 50, 75, 100};
        BigDecimal progress = project.getOverallProgress();

        for (int milestone : milestones) {
            BigDecimal milestoneDecimal = new BigDecimal(milestone);

            if (progress.compareTo(milestoneDecimal) >= 0) {
                // ✅ PROJECT MILESTONE COMPLETED EVENT
                ProjectMilestoneCompletedEvent milestoneEvent = ProjectMilestoneCompletedEvent.builder()
                        .projectId(project.getId())
                        .companyId(project.getCompanyId())
                        .milestoneName(milestone + "% Completion")
                        .milestoneDescription("Project reached " + milestone + "% completion")
                        .targetPercentage(milestone)
                        .actualProgress(progress)
                        .completionDate(LocalDate.now())
                        .timestamp(LocalDateTime.now())
                        .build();

                kafkaTemplate.send("project.milestone.completed", milestoneEvent);
                break;
            }
        }
    }

    private void checkDelays(Project project) {
        if (project.getPlannedEndDate() == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate plannedEnd = project.getPlannedEndDate();

        if (today.isAfter(plannedEnd) && project.getOverallProgress().compareTo(new BigDecimal("100")) < 0) {
            long delayDays = ChronoUnit.DAYS.between(plannedEnd, today);

            List<String> delayedPhases = project.getPhases().stream()
                    .filter(phase -> phase.getStatus() == PhaseStatus.DELAYED ||
                            (phase.getPlannedEndDate() != null && LocalDate.now().isAfter(phase.getPlannedEndDate())))
                    .map(ProjectPhase::getPhaseName)
                    .collect(Collectors.toList());

            // ✅ PROJECT DELAYED EVENT
            ProjectDelayedEvent delayEvent = ProjectDelayedEvent.builder()
                    .projectId(project.getId())
                    .companyId(project.getCompanyId())
                    .projectName(project.getName())
                    .plannedEndDate(plannedEnd)
                    .currentEstimate(today.plusDays(30))
                    .delayDays((int) delayDays)
                    .reason("Project behind schedule")
                    .currentProgress(project.getOverallProgress())
                    .delayedPhases(delayedPhases)
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("project.delayed", delayEvent);
        }
    }
}