package com.strux.task_service.controller;

import com.strux.task_service.dto.*;
import com.strux.task_service.enums.*;
import com.strux.task_service.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @RequestBody @Valid TaskCreateRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        log.info("Creating task: {} by user: {}", request.getTitle(), userId);
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
            @PathVariable TaskStatus status
    ) {
        List<TaskDto> tasks = taskService.getTasksByStatus(companyId, status);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/company/{companyId}/priority/{priority}")
    public ResponseEntity<List<TaskDto>> getTasksByPriority(
            @PathVariable String companyId,
            @PathVariable TaskPriority priority
    ) {
        List<TaskDto> tasks = taskService.getTasksByPriority(companyId, priority);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/company/{companyId}/type/{type}")
    public ResponseEntity<List<TaskDto>> getTasksByType(
            @PathVariable String companyId,
            @PathVariable TaskType type
    ) {
        List<TaskDto> tasks = taskService.getTasksByType(companyId, type);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}/subtasks")
    public ResponseEntity<SubtaskDto> getSubtasks(@PathVariable String taskId) {
        SubtaskDto subtasks = taskService.getSubtasks(taskId);
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
        List<TaskDto> templates = taskService.getTaskTemplates(companyId);
        return ResponseEntity.ok(templates);
    }

    @PostMapping("/search")
    public ResponseEntity<List<TaskDto>> searchTasks(@RequestBody TaskSearchRequest request) {
        List<TaskDto> tasks = taskService.searchTasks(request);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable String taskId,
            @RequestBody @Valid TaskUpdateRequest request
    ) {
        TaskDto task = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{taskId}/assign")
    public ResponseEntity<TaskDto> assignTask(
            @PathVariable String taskId,
            @RequestBody @Valid TaskAssignRequest request
    ) {
        TaskDto task = taskService.assignTask(taskId, request);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{taskId}/progress")
    public ResponseEntity<TaskDto> updateProgress(
            @PathVariable String taskId,
            @RequestBody @Valid TaskProgressUpdateRequest request
    ) {
        TaskDto task = taskService.updateProgress(taskId, request);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{taskId}/complete")
    public ResponseEntity<TaskDto> completeTask(@PathVariable String taskId) {
        TaskDto task = taskService.completeTask(taskId);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable String taskId,
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        taskService.deleteTask(taskId, hardDelete);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/company/{companyId}/stats")
    public ResponseEntity<TaskStatsResponse> getTaskStats(@PathVariable String companyId) {
        TaskStatsResponse stats = taskService.getTaskStats(companyId);
        return ResponseEntity.ok(stats);
    }
}
