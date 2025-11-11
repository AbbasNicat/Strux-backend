package com.strux.project_service.controller;

import com.strux.project_service.dto.*;
import com.strux.project_service.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        log.info("Creating new project: {}", request.getName());
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        log.info("Fetching all projects");
        List<ProjectResponse> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetailResponse> getProjectDetail(@PathVariable String projectId) {
        log.info("Fetching project detail: {}", projectId);
        ProjectDetailResponse response = projectService.getProjectDetailById(projectId, null);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable String projectId,
            @Valid @RequestBody UpdateProjectRequest request) {
        log.info("Updating project: {}", projectId);
        ProjectResponse response = projectService.updateProject(request, projectId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectMapResponse> partialUpdateProject(
            @PathVariable String projectId,
            @RequestBody Map<String, Object> updates
    ) {
        log.info("Partial update for project: {} with fields: {}", projectId, updates.keySet());
        ProjectMapResponse updated = projectService.partialUpdateProject(projectId, updates);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectId) {
        log.info("Deleting project: {}", projectId);
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/progress")
    public ResponseEntity<ProjectProgressResponse> getProjectProgress(@PathVariable String projectId) {
        log.info("Fetching project progress: {}", projectId);
        ProjectProgressResponse response = projectService.getProjectProgress(projectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/map")
    public ResponseEntity<List<ProjectMapResponse>> getProjectsForMap() {
        log.info("Fetching projects for map view");
        List<ProjectMapResponse> projects = projectService.getProjectsForMap();
        return ResponseEntity.ok(projects);
    }

    @PostMapping("/{projectId}/phases")
    public ResponseEntity<Void> addPhase(
            @PathVariable String projectId,
            @Valid @RequestBody AddPhaseRequest request) {
        log.info("Adding phase to project: {}", projectId);
        projectService.addPhase(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{projectId}/phases/{phaseId}/progress")
    public ResponseEntity<Void> updatePhaseProgress(
            @PathVariable String projectId,
            @PathVariable String phaseId,
            @Valid @RequestBody UpdateProgressRequest request) {
        log.info("Updating phase progress - Project: {}, Phase: {}", projectId, phaseId);
        projectService.updatePhaseProgress(projectId, phaseId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectId}/phases/{phaseId}")
    public ResponseEntity<Void> deletePhase(
            @PathVariable String projectId,
            @PathVariable String phaseId) {
        log.info("Deleting phase - Project: {}, Phase: {}", projectId, phaseId);
        projectService.deletePhase(projectId, phaseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<ProjectResponse>> getProjectsByCompanyId(@PathVariable String companyId) {
        log.info("Fetching projects by company ID: {}", companyId);
        List<ProjectResponse> response = projectService.getProjectsByCompanyId(companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{projectId}/company-id")
    public ResponseEntity<String> getCompanyIdByProject(@PathVariable String projectId) {
        log.info("Fetching companyId for project {}", projectId);
        String companyId = projectService.getCompanyIdByProjectId(projectId);
        return ResponseEntity.ok(companyId);
    }
}
