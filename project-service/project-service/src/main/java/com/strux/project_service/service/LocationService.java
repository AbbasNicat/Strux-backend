package com.strux.project_service.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import com.google.maps.model.*;
import com.strux.project_service.dto.*;
import com.strux.project_service.model.Project;
import com.strux.project_service.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    private final ProjectRepository projectRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    private final Map<String, CompletableFuture<Object>> pendingRequests = new ConcurrentHashMap<>();

    private static final String TOPIC_COMPANY_REQUEST = "company-info-request";
    private static final String TOPIC_COMPANY_RESPONSE = "company-info-response";
    private static final String TOPIC_PHASE_REQUEST = "phase-info-request";
    private static final String TOPIC_PHASE_RESPONSE = "phase-info-response";
    private static final String TOPIC_WORKER_REQUEST = "worker-info-request";
    private static final String TOPIC_WORKER_RESPONSE = "worker-info-response";
    private static final String TOPIC_MEDIA_REQUEST = "media-request";
    private static final String TOPIC_MEDIA_RESPONSE = "media-response";

    // Event Topics
    private static final String TOPIC_LOCATION_CREATED = "location.created";
    private static final String TOPIC_LOCATION_ASSIGNED = "location.assigned";
    private static final String TOPIC_LOCATION_UPDATED = "location.updated";

    public List<LocationSuggestionDTO> searchLocation(String query) {
        try {
            String url = "https://places.googleapis.com/v1/places:autocomplete";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Goog-Api-Key", apiKey);

            Map<String, Object> body = Map.of(
                    "input", query,
                    "languageCode", "en"
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            log.info("📦 API Response: {}", responseBody);

            List<Map<String, Object>> suggestions =
                    (List<Map<String, Object>>) responseBody.get("suggestions");

            if (suggestions == null || suggestions.isEmpty()) {
                log.warn("⚠️ No suggestions found");
                return new ArrayList<>();
            }

            return suggestions.stream()
                    .map(suggestion -> {
                        Map<String, Object> placePrediction =
                                (Map<String, Object>) suggestion.get("placePrediction");

                        Map<String, Object> text =
                                (Map<String, Object>) placePrediction.get("text");

                        Map<String, Object> structuredFormat =
                                (Map<String, Object>) placePrediction.get("structuredFormat");

                        Map<String, Object> mainText =
                                (Map<String, Object>) structuredFormat.get("mainText");

                        Map<String, Object> secondaryText =
                                (Map<String, Object>) structuredFormat.get("secondaryText");

                        return LocationSuggestionDTO.builder()
                                .placeId((String) placePrediction.get("placeId"))
                                .description((String) text.get("text"))
                                .mainText((String) mainText.get("text"))
                                .secondaryText((String) secondaryText.get("text"))
                                .build();
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ Location search error: ", e);
            throw new RuntimeException("Search failed: " + e.getMessage());
        }
    }

    // ✅ YENİ PLACES API (New) - Place Details
    public LocationDetailDTO getLocationDetails(String placeId) {
        try {
            String url = "https://places.googleapis.com/v1/places/" + placeId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Goog-Api-Key", apiKey);
            headers.set("X-Goog-FieldMask", "id,displayName,formattedAddress,location,addressComponents");

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            Map<String, Object> place = response.getBody();

            log.info("📍 Place Details: {}", place);

            Map<String, Object> location = (Map<String, Object>) place.get("location");
            Map<String, Object> displayName = (Map<String, Object>) place.get("displayName");

            List<Map<String, Object>> addressComponents =
                    (List<Map<String, Object>>) place.get("addressComponents");

            LocationDetailDTO details = LocationDetailDTO.builder()
                    .placeId(placeId)
                    .formattedAddress((String) place.get("formattedAddress"))
                    .latitude((Double) location.get("latitude"))
                    .longitude((Double) location.get("longitude"))
                    .city(extractComponentFromNew(addressComponents, "locality"))
                    .country(extractComponentFromNew(addressComponents, "country"))
                    .build();

            publishLocationCreatedEvent(details);
            return details;

        } catch (Exception e) {
            log.error("❌ Location details error: ", e);
            throw new RuntimeException("Details failed: " + e.getMessage());
        }
    }

    private String extractComponentFromNew(List<Map<String, Object>> components, String type) {
        if (components == null) return null;

        return components.stream()
                .filter(comp -> {
                    List<String> types = (List<String>) comp.get("types");
                    return types != null && types.contains(type);
                })
                .map(comp -> (String) comp.get("longText"))
                .findFirst()
                .orElse(null);
    }

    private String extractCityFromComponents(List<Map<String, Object>> components) {
        return components.stream()
                .filter(comp -> {
                    List<String> types = (List<String>) comp.get("types");
                    return types.contains("locality");
                })
                .map(comp -> (String) comp.get("long_name"))
                .findFirst()
                .orElse(null);
    }

    private String extractCountryFromComponents(List<Map<String, Object>> components) {
        return components.stream()
                .filter(comp -> {
                    List<String> types = (List<String>) comp.get("types");
                    return types.contains("country");
                })
                .map(comp -> (String) comp.get("long_name"))
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public Project saveProjectLocation(String projectId, CreateLocationRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        boolean isNewLocation = project.getLocation() == null;
        boolean isUpdate = !isNewLocation;

        Project.ProjectLocation oldLocation = null;
        if (isUpdate) {
            oldLocation = copyLocation(project.getLocation());
        }

        Project.ProjectLocation location = project.getLocation();
        if (location == null) {
            location = new Project.ProjectLocation();
        }

        location.setPlaceId(request.getPlaceId());
        location.setAddress(request.getFormattedAddress());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setCity(request.getCity());
        location.setCountry(request.getCountry());

        project.setLocation(location);
        Project savedProject = projectRepository.save(project);

        // Event publishing
        if (isNewLocation) {
            publishLocationAssignedEvent(savedProject, location);
        } else {
            publishLocationUpdatedEvent(savedProject, oldLocation, location);
        }

        return savedProject;
    }

    public List<ProjectMarkerDTO> getAllProjectMarkers(MapBoundsRequest bounds) {
        List<Project> projects;

        if (bounds != null) {
            projects = projectRepository.findByLocationBounds(
                    bounds.getSouthWestLat(),
                    bounds.getSouthWestLng(),
                    bounds.getNorthEastLat(),
                    bounds.getNorthEastLng()
            );
        } else {
            projects = projectRepository.findAll();
        }

        return projects.stream()
                .filter(p -> p.getLocation() != null)
                .map(this::convertToMarker)
                .collect(Collectors.toList());
    }

    public CompletableFuture<ProjectMapResponse> getProjectMapDetails(String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        if (project.getLocation() == null) {
            throw new RuntimeException("Proje konumu bulunamadı");
        }

        Project.ProjectLocation loc = project.getLocation();

        LocationInfoDTO locationInfo = LocationInfoDTO.builder()
                .latitude(String.valueOf(loc.getLatitude()))
                .longitude(String.valueOf(loc.getLongitude()))
                .address(loc.getAddress())
                .city(loc.getCity())
                .district(loc.getDistrict())
                .country(loc.getCountry())
                .placeId(loc.getPlaceId())
                .build();

        ProjectBasicInfoDTO basicInfo = createBasicInfo(project);

        return CompletableFuture.supplyAsync(() -> {
            return ProjectMapResponse.builder()
                    .id(project.getId())
                    .name(project.getName())
                    .type(project.getType())
                    .status(project.getStatus())
                    .latitude(String.valueOf(loc.getLatitude()))
                    .longitude(String.valueOf(loc.getLongitude()))
                    .address(loc.getAddress())
                    .city(loc.getCity())
                    .basicInfo(basicInfo)
                    .locationInfo(locationInfo)
                    .overallProgress(project.getOverallProgress())
                    .imageUrl(project.getImageUrl())
                    .statusColor(getStatusColor(project.getStatus()))
                    .build();
        });
    }

    private ProjectBasicInfoDTO createBasicInfo(Project project) {
        LocalDateTime plannedEnd = project.getPlannedEndDate() != null ?
                project.getPlannedEndDate().atStartOfDay() : null;
        LocalDateTime actualEnd = project.getActualEndDate() != null ?
                project.getActualEndDate().atStartOfDay() : null;

        return ProjectBasicInfoDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .type(project.getType().name())
                .status(project.getStatus().name())
                .startDate(project.getStartDate() != null ? project.getStartDate().atStartOfDay() : null)
                .endDate(project.getPlannedEndDate() != null ? project.getPlannedEndDate().atStartOfDay() : null)
                .estimatedEndDate(project.getEstimatedEndDate() != null ? project.getEstimatedEndDate().atStartOfDay() : null) // ⬅️ EKLE
                .actualEndDate(project.getActualEndDate() != null ? project.getActualEndDate().atStartOfDay() : null)
                .totalBudget(project.getTotalBudget())
                .spentBudget(project.getSpentBudget())
                .completionPercentage(project.getOverallProgress() != null ? project.getOverallProgress().intValue() : 0)
                .build();
    }

    public List<ProjectMarkerDTO> getNearbyProjects(Double latitude, Double longitude, Double radiusKm) {
        List<Project> nearbyProjects = projectRepository
                .findNearbyProjects(latitude, longitude, radiusKm);

        return nearbyProjects.stream()
                .filter(p -> p.getLocation() != null)
                .map(this::convertToMarker)
                .collect(Collectors.toList());
    }

    public List<ProjectMarkerDTO> getFilteredProjects(ProjectMapFilterRequest filter) {
        List<Project> projects = projectRepository.findByFilters(
                filter.getStatuses(),
                filter.getTypes(),
                filter.getCompanyIds(),
                filter.getMinCompletion(),
                filter.getMaxCompletion()
        );

        return projects.stream()
                .filter(p -> p.getLocation() != null)
                .map(this::convertToMarker)
                .collect(Collectors.toList());
    }

    private void publishLocationCreatedEvent(LocationDetailDTO location) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "LOCATION_CREATED");
        event.put("timestamp", LocalDateTime.now().toString());
        event.put("placeId", location.getPlaceId());
        event.put("formattedAddress", location.getFormattedAddress());
        event.put("latitude", location.getLatitude());
        event.put("longitude", location.getLongitude());
        event.put("city", location.getCity());
        event.put("country", location.getCountry());

        kafkaTemplate.send(TOPIC_LOCATION_CREATED, event);
        log.info("Location created event published for placeId: {}", location.getPlaceId());
    }

    private void publishLocationAssignedEvent(Project project, Project.ProjectLocation location) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "LOCATION_ASSIGNED");
        event.put("timestamp", LocalDateTime.now().toString());
        event.put("projectId", project.getId());
        event.put("projectName", project.getName());
        event.put("companyId", project.getCompanyId());
        event.put("placeId", location.getPlaceId());
        event.put("formattedAddress", location.getAddress());
        event.put("latitude", location.getLatitude());
        event.put("longitude", location.getLongitude());
        event.put("city", location.getCity());
        event.put("country", location.getCountry());

        kafkaTemplate.send(TOPIC_LOCATION_ASSIGNED, event);
        log.info("Location assigned event published for project: {}", project.getId());
    }

    private void publishLocationUpdatedEvent(
            Project project,
            Project.ProjectLocation oldLocation,
            Project.ProjectLocation newLocation) {

        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "LOCATION_UPDATED");
        event.put("timestamp", LocalDateTime.now().toString());
        event.put("projectId", project.getId());
        event.put("projectName", project.getName());
        event.put("companyId", project.getCompanyId());

        Map<String, Object> oldData = new HashMap<>();
        if (oldLocation != null) {
            oldData.put("placeId", oldLocation.getPlaceId());
            oldData.put("formattedAddress", oldLocation.getAddress());
            oldData.put("latitude", oldLocation.getLatitude());
            oldData.put("longitude", oldLocation.getLongitude());
            oldData.put("city", oldLocation.getCity());
            oldData.put("country", oldLocation.getCountry());
        }
        event.put("oldLocation", oldData);

        Map<String, Object> newData = new HashMap<>();
        newData.put("placeId", newLocation.getPlaceId());
        newData.put("formattedAddress", newLocation.getAddress());
        newData.put("latitude", newLocation.getLatitude());
        newData.put("longitude", newLocation.getLongitude());
        newData.put("city", newLocation.getCity());
        newData.put("country", newLocation.getCountry());
        event.put("newLocation", newData);

        Map<String, Boolean> changes = new HashMap<>();

        boolean coordinatesChanged = false;
        if (oldLocation != null) {
            coordinatesChanged = !Objects.equals(oldLocation.getLatitude(), newLocation.getLatitude()) ||
                    !Objects.equals(oldLocation.getLongitude(), newLocation.getLongitude());
        } else {
            coordinatesChanged = true;
        }

        boolean addressChanged = !Objects.equals(
                oldLocation != null ? oldLocation.getAddress() : null,
                newLocation.getAddress()
        );

        boolean cityChanged = !Objects.equals(
                oldLocation != null ? oldLocation.getCity() : null,
                newLocation.getCity()
        );

        changes.put("coordinatesChanged", coordinatesChanged);
        changes.put("addressChanged", addressChanged);
        changes.put("cityChanged", cityChanged);
        event.put("changes", changes);

        kafkaTemplate.send(TOPIC_LOCATION_UPDATED, event);
        log.info("Location updated event published for project: {}", project.getId());
    }

    private CompletableFuture<CompanyInfoDTO> requestCompanyInfo(String companyId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<Object> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        Map<String, String> request = new HashMap<>();
        request.put("companyId", companyId);
        request.put("correlationId", correlationId);

        kafkaTemplate.send(TOPIC_COMPANY_REQUEST, request);

        return future.thenApply(obj -> (CompanyInfoDTO) obj)
                .orTimeout(5, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("Company info request timeout", ex);
                    return CompanyInfoDTO.builder()
                            .name("Bilinmiyor")
                            .build();
                });
    }

    private CompletableFuture<CurrentPhaseDTO> requestCurrentPhase(String projectId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<Object> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        Map<String, Object> request = new HashMap<>();
        request.put("projectId", projectId);
        request.put("correlationId", correlationId);
        request.put("currentOnly", true);

        kafkaTemplate.send(TOPIC_PHASE_REQUEST, request);

        return future.thenApply(obj -> (CurrentPhaseDTO) obj)
                .orTimeout(5, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("Current phase request timeout", ex);
                    return null;
                });
    }

    private CompletableFuture<List<PhaseProgressDTO>> requestAllPhases(String projectId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<Object> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        Map<String, Object> request = new HashMap<>();
        request.put("projectId", projectId);
        request.put("correlationId", correlationId);
        request.put("currentOnly", false);

        kafkaTemplate.send(TOPIC_PHASE_REQUEST, request);

        return future.thenApply(obj -> (List<PhaseProgressDTO>) obj)
                .orTimeout(5, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("All phases request timeout", ex);
                    return new ArrayList<>();
                });
    }

    private CompletableFuture<List<WorkerInfoDTO>> requestActiveWorkers(String projectId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<Object> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        Map<String, String> request = new HashMap<>();
        request.put("projectId", projectId);
        request.put("correlationId", correlationId);

        kafkaTemplate.send(TOPIC_WORKER_REQUEST, request);

        return future.thenApply(obj -> (List<WorkerInfoDTO>) obj)
                .orTimeout(5, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("Workers request timeout", ex);
                    return new ArrayList<>();
                });
    }

    private CompletableFuture<List<ProjectMediaDTO>> requestRecentMedia(String projectId, int limit) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<Object> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        Map<String, Object> request = new HashMap<>();
        request.put("projectId", projectId);
        request.put("limit", limit);
        request.put("correlationId", correlationId);

        kafkaTemplate.send(TOPIC_MEDIA_REQUEST, request);

        return future.thenApply(obj -> (List<ProjectMediaDTO>) obj)
                .orTimeout(5, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("Media request timeout", ex);
                    return new ArrayList<>();
                });
    }


    @KafkaListener(topics = TOPIC_COMPANY_RESPONSE, groupId = "project-service")
    public void handleCompanyResponse(Map<String, Object> response) {
        String correlationId = (String) response.get("correlationId");
        CompletableFuture<Object> future = pendingRequests.remove(correlationId);
        if (future != null) {
            CompanyInfoDTO company = (CompanyInfoDTO) response.get("company");
            future.complete(company);
        }
    }

    @KafkaListener(topics = TOPIC_PHASE_RESPONSE, groupId = "project-service")
    public void handlePhaseResponse(Map<String, Object> response) {
        String correlationId = (String) response.get("correlationId");
        CompletableFuture<Object> future = pendingRequests.remove(correlationId);
        if (future != null) {
            Boolean currentOnly = (Boolean) response.get("currentOnly");
            if (Boolean.TRUE.equals(currentOnly)) {
                CurrentPhaseDTO phase = (CurrentPhaseDTO) response.get("currentPhase");
                future.complete(phase);
            } else {
                List<PhaseProgressDTO> phases = (List<PhaseProgressDTO>) response.get("allPhases");
                future.complete(phases);
            }
        }
    }

    @KafkaListener(topics = TOPIC_WORKER_RESPONSE, groupId = "project-service")
    public void handleWorkerResponse(Map<String, Object> response) {
        String correlationId = (String) response.get("correlationId");
        CompletableFuture<Object> future = pendingRequests.remove(correlationId);
        if (future != null) {
            List<WorkerInfoDTO> workers = (List<WorkerInfoDTO>) response.get("workers");
            future.complete(workers);
        }
    }

    @KafkaListener(topics = TOPIC_MEDIA_RESPONSE, groupId = "project-service")
    public void handleMediaResponse(Map<String, Object> response) {

        String correlationId = (String) response.get("correlationId");
        CompletableFuture<Object> future = pendingRequests.remove(correlationId);
        if (future != null) {
            List<ProjectMediaDTO> media = (List<ProjectMediaDTO>) response.get("media");
            future.complete(media);
        }
    }

    private ProjectMarkerDTO convertToMarker(Project project) {

        Project.ProjectLocation loc = project.getLocation();

        return ProjectMarkerDTO.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .status(project.getStatus().name())
                .type(project.getType().name())
                .completionPercentage(project.getOverallProgress() != null ?
                        project.getOverallProgress().intValue() : 0)
                .companyName("")
                .address(loc.getAddress())
                .build();
    }

    private ProjectProgressResponse calculateStatistics(

            Project project,
            List<WorkerInfoDTO> workers) {

        return ProjectProgressResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .overallProgress(project.getOverallProgress())
                .startDate(project.getStartDate())
                .plannedEndDate(project.getPlannedEndDate())
                .estimatedEndDate(calculateEstimatedEndDate(project))
                .totalPhases(0)
                .completedPhases(0)
                .inProgressPhases(0)
                .phaseProgresses(new ArrayList<>())
                .build();
    }

    private LocalDate calculateEstimatedEndDate(Project project) {
        if (project.getPlannedEndDate() == null) return null;
        if (project.getOverallProgress() == null || project.getOverallProgress().compareTo(BigDecimal.ZERO) == 0) {
            return project.getPlannedEndDate();
        }

        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(
                project.getStartDate(), project.getPlannedEndDate());
        long elapsedDays = calculateDaysElapsed(project.getStartDate());

        double progressPercent = project.getOverallProgress().doubleValue();
        if (progressPercent > 0) {
            long estimatedTotalDays = (long) (elapsedDays / progressPercent * 100);
            return project.getStartDate().plusDays(estimatedTotalDays);
        }

        return project.getPlannedEndDate();
    }

    private String extractAddressComponent(GeocodingResult result, String type) {

        for (AddressComponent component : result.addressComponents) {
            for (AddressComponentType componentType : component.types) {
                if (componentType.name().equalsIgnoreCase(type)) {
                    return component.longName;
                }
            }
        }
        return null;
    }

    private int calculateDaysElapsed(java.time.LocalDate startDate) {

        if (startDate == null) return 0;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(
                startDate, java.time.LocalDate.now());
    }

    private int calculateDaysRemaining(java.time.LocalDate endDate) {

        if (endDate == null) return 0;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(), endDate);
    }

    private String getStatusColor(com.strux.project_service.enums.ProjectStatus status) {
        switch (status) {
            case COMPLETED: return "#4CAF50";
            case IN_PROGRESS: return "#2196F3";
            case PLANNING: return "#FF9800";
            case ON_HOLD: return "#FFC107";
            case CANCELLED: return "#F44336";
            default: return "#808080";
        }
    }

    private Project.ProjectLocation copyLocation(Project.ProjectLocation location) {
        if (location == null) return null;

        Project.ProjectLocation copy = new Project.ProjectLocation();
        copy.setPlaceId(location.getPlaceId());
        copy.setAddress(location.getAddress());
        copy.setLatitude(location.getLatitude());
        copy.setLongitude(location.getLongitude());
        copy.setCity(location.getCity());
        copy.setCountry(location.getCountry());

        return copy;
    }
}