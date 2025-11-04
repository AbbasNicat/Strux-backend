package com.strux.project_service.controller;

import com.strux.project_service.dto.*;
import com.strux.project_service.model.Project;
import com.strux.project_service.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/search")
    public ResponseEntity<List<LocationSuggestionDTO>> searchLocation(
            @RequestParam String query) {
        log.info("Searching location with query: {}", query);
        try {
            List<LocationSuggestionDTO> suggestions = locationService.searchLocation(query);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("Error searching location", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/details/{placeId}")
    public ResponseEntity<LocationDetailDTO> getLocationDetails(
            @PathVariable String placeId) {
        log.info("Fetching location details for placeId: {}", placeId);
        try {
            LocationDetailDTO details = locationService.getLocationDetails(placeId);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            log.error("Error fetching location details", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @PostMapping("/projects/{projectId}")
    public ResponseEntity<Project> saveProjectLocation(
            @PathVariable String projectId,
            @Valid @RequestBody CreateLocationRequest request) {
        log.info("Saving location for project: {}", projectId);
        try {
            Project project = locationService.saveProjectLocation(projectId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(project);
        } catch (RuntimeException e) {
            log.error("Error saving project location", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/markers")
    public ResponseEntity<List<ProjectMarkerDTO>> getAllProjectMarkers(
            @RequestParam(required = false) Double southWestLat,
            @RequestParam(required = false) Double southWestLng,
            @RequestParam(required = false) Double northEastLat,
            @RequestParam(required = false) Double northEastLng) {

        log.info("Fetching project markers with bounds: SW({}, {}), NE({}, {})",
                southWestLat, southWestLng, northEastLat, northEastLng);

        try {
            MapBoundsRequest bounds = null;

            if (southWestLat != null && southWestLng != null &&
                    northEastLat != null && northEastLng != null) {
                bounds = MapBoundsRequest.builder()
                        .southWestLat(southWestLat)
                        .southWestLng(southWestLng)
                        .northEastLat(northEastLat)
                        .northEastLng(northEastLng)
                        .build();
            }

            List<ProjectMarkerDTO> markers = locationService.getAllProjectMarkers(bounds);
            return ResponseEntity.ok(markers);
        } catch (Exception e) {
            log.error("Error fetching project markers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/projects/{projectId}/map-details")
    public CompletableFuture<ResponseEntity<ProjectMapResponse>> getProjectMapDetails(
            @PathVariable String projectId) {
        log.info("Fetching detailed map info for project: {}", projectId);

        return locationService.getProjectMapDetails(projectId)
                .thenApply(response -> {
                    log.info("Successfully fetched map details for project: {}", projectId);
                    return ResponseEntity.ok(response);
                })
                .exceptionally(ex -> {
                    log.error("Error fetching project map details for project: {}", projectId, ex);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }


    @GetMapping("/nearby")
    public ResponseEntity<List<ProjectMarkerDTO>> getNearbyProjects(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radiusKm) {

        log.info("Fetching nearby projects - Lat: {}, Lng: {}, Radius: {}km",
                latitude, longitude, radiusKm);

        try {
            List<ProjectMarkerDTO> nearbyProjects = locationService.getNearbyProjects(
                    latitude, longitude, radiusKm);
            return ResponseEntity.ok(nearbyProjects);
        } catch (Exception e) {
            log.error("Error fetching nearby projects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/filter")
    public ResponseEntity<List<ProjectMarkerDTO>> getFilteredProjects(
            @Valid @RequestBody ProjectMapFilterRequest filter) {

        log.info("Fetching filtered projects - Statuses: {}, Types: {}, Companies: {}",
                filter.getStatuses(), filter.getTypes(), filter.getCompanyIds());

        try {
            List<ProjectMarkerDTO> filteredProjects = locationService.getFilteredProjects(filter);
            return ResponseEntity.ok(filteredProjects);
        } catch (Exception e) {
            log.error("Error fetching filtered projects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
