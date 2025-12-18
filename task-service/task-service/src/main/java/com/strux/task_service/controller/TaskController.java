package com.strux.task_service.controller;

import com.strux.task_service.dto.*;
import com.strux.task_service.enums.*;
import com.strux.task_service.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        TaskDto task = taskService.createTask(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDto> getTask(@PathVariable String taskId) {
        TaskDto task = taskService.getTask(taskId);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<TaskDto>> getTasksByCompany(@PathVariable String companyId) {
        List<TaskDto> tasks = taskService.getTasksByCompany(companyId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskDto>> getTasksByProject(@PathVariable String projectId) {
        List<TaskDto> tasks = taskService.getTasksByProject(projectId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/creator/{userId}")
    public ResponseEntity<List<TaskDto>> getTasksByCreator(@PathVariable String userId) {
        List<TaskDto> tasks = taskService.getTasksByCreator(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/assigned/{userId}")
    public ResponseEntity<List<TaskDto>> getTasksAssignedToUser(@PathVariable String userId) {
        List<TaskDto> tasks = taskService.getTasksAssignedToUser(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/company/{companyId}/status/{status}")
    public ResponseEntity<List<TaskDto>> getTasksByStatus(
            @PathVariable String companyId,
            @PathVariable TaskStatus status) {
        List<TaskDto> tasks = taskService.getTasksByStatus(companyId, status);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/company/{companyId}/priority/{priority}")
    public ResponseEntity<List<TaskDto>> getTasksByPriority(
            @PathVariable String companyId,
            @PathVariable TaskPriority priority) {
        List<TaskDto> tasks = taskService.getTasksByPriority(companyId, priority);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/company/{companyId}/type/{type}")
    public ResponseEntity<List<TaskDto>> getTasksByType(
            @PathVariable String companyId,
            @PathVariable TaskType type) {
        List<TaskDto> tasks = taskService.getTasksByType(companyId, type);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{parentTaskId}/subtasks")
    public ResponseEntity<SubtaskDto> getSubtasks(@PathVariable String parentTaskId) {
        SubtaskDto subtasks = taskService.getSubtasks(parentTaskId);
        return ResponseEntity.ok(subtasks);
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<TaskDto>> getTasksByAsset(@PathVariable String assetId) {
        List<TaskDto> tasks = taskService.getTasksByAsset(assetId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<List<TaskDto>> getTasksByEquipment(@PathVariable String equipmentId) {
        List<TaskDto> tasks = taskService.getTasksByEquipment(equipmentId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<TaskDto>> getTasksByLocation(@PathVariable String locationId) {
        List<TaskDto> tasks = taskService.getTasksByLocation(locationId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/company/{companyId}/overdue")
    public ResponseEntity<List<TaskDto>> getOverdueTasks(@PathVariable String companyId) {
        List<TaskDto> tasks = taskService.getOverdueTasks(companyId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/company/{companyId}/recurring")
    public ResponseEntity<List<TaskDto>> getRecurringTasks(@PathVariable String companyId) {
        List<TaskDto> tasks = taskService.getRecurringTasks(companyId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/company/{companyId}/templates")
    public ResponseEntity<List<TaskDto>> getTaskTemplates(@PathVariable String companyId) {
        List<TaskDto> tasks = taskService.getTaskTemplates(companyId);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/search")
    public ResponseEntity<List<TaskDto>> searchTasks(@Valid @RequestBody TaskSearchRequest request) {
        List<TaskDto> tasks = taskService.searchTasks(request);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/project/{projectId}/stats")
    public ResponseEntity<ProjectTaskStatsResponse> getProjectTaskStats(@PathVariable String projectId) {
        ProjectTaskStatsResponse stats = taskService.getProjectTaskStats(projectId);
        return ResponseEntity.ok(stats);
    }

    // ✅ UPDATED: userId parametresi eklendi
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable String taskId,
            @Valid @RequestBody TaskUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        TaskDto task = taskService.updateTask(taskId, request, userId);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{taskId}/assign")
    public ResponseEntity<TaskDto> assignTask(
            @PathVariable String taskId,
            @Valid @RequestBody TaskAssignRequest request) {
        TaskDto task = taskService.assignTask(taskId, request);
        return ResponseEntity.ok(task);
    }

    // ✅ UPDATED: userId parametresi eklendi
    @PutMapping("/{taskId}/progress")
    public ResponseEntity<TaskDto> updateProgress(
            @PathVariable String taskId,
            @Valid @RequestBody TaskProgressUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        TaskDto task = taskService.updateProgress(taskId, request, userId);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{taskId}/complete")
    public ResponseEntity<TaskDto> completeTask(
            @PathVariable String taskId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        TaskDto task = taskService.completeTask(taskId, userId);
        return ResponseEntity.ok(task);
    }

    // ✅ YENİ ENDPOINT: Task Approval
    @PostMapping("/{taskId}/approve")
    public ResponseEntity<TaskDto> approveTask(
            @PathVariable String taskId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        TaskDto task = taskService.approveTask(taskId, userId);
        return ResponseEntity.ok(task);
    }

    // ✅ YENİ ENDPOINT: Task Rejection
    @PostMapping("/{taskId}/reject")
    public ResponseEntity<TaskDto> rejectTask(
            @PathVariable String taskId,
            @Valid @RequestBody TaskRejectRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        TaskDto task = taskService.rejectTask(taskId, userId, request.getRejectionReason());
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable String taskId,
            @RequestParam(defaultValue = "false") boolean hardDelete) {
        taskService.deleteTask(taskId, hardDelete);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/company/{companyId}/stats")
    public ResponseEntity<TaskStatsResponse> getTaskStats(@PathVariable String companyId) {
        TaskStatsResponse stats = taskService.getTaskStats(companyId);
        return ResponseEntity.ok(stats);
    }

    // ✅ YENİ ENDPOINT
    @GetMapping("/unit/{unitId}")
    public ResponseEntity<List<TaskDto>> getTasksByUnit(@PathVariable String unitId) {
        List<TaskDto> tasks = taskService.getTasksByUnit(unitId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/active-count")
    public ResponseEntity<Integer> countActiveTasks(@RequestParam String projectId) {
        return ResponseEntity.ok(taskService.countActiveTasksByProject(projectId));
    }

}