package com.strux.task_service.service;

import com.strux.task_service.dto.*;
import com.strux.task_service.enums.*;
import com.strux.task_service.kafka.TaskEventProducer;
import com.strux.task_service.model.Task;
import com.strux.task_service.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TaskEventProducer taskEventProducer;

    @Transactional
    public TaskDto createTask(TaskCreateRequest request, String userId) {
        log.info("Creating task: {} by user: {}", request.getTitle(), userId);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .companyId(request.getCompanyId())
                .projectId(request.getProjectId())
                .createdBy(userId)
                .assignedTo(request.getAssignedTo())
                .assignees(request.getAssignees())
                .status(request.getStatus())
                .priority(request.getPriority())
                .type(request.getType())
                .category(request.getCategory())
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .estimatedHours(request.getEstimatedHours())
                .progressPercentage(0)
                .parentTaskId(request.getParentTaskId())
                .dependsOn(request.getDependsOn())
                .assetId(request.getAssetId())
                .equipmentId(request.getEquipmentId())
                .locationId(request.getLocationId())
                .attachmentIds(request.getAttachmentIds())
                .tags(request.getTags())
                .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                .recurrencePattern(request.getRecurrencePattern())
                .build();

        task = taskRepository.save(task);

        taskEventProducer.publishTaskCreatedEvent(task);

        return toDto(task);
    }

    public TaskDto getTask(String taskId) {
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        taskRepository.save(task);

        return toDto(task);
    }

    public List<TaskDto> getTasksByCompany(String companyId) {
        return taskRepository.findByCompanyIdAndDeletedAtIsNull(companyId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getTasksByProject(String projectId) {
        return taskRepository.findByProjectIdAndDeletedAtIsNull(projectId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getTasksByCreator(String userId) {
        return taskRepository.findByCreatedByAndDeletedAtIsNull(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getTasksAssignedToUser(String userId) {
        List<Task> assignedTasks = taskRepository.findByAssignedToAndDeletedAtIsNull(userId);
        List<Task> assigneesTasks = taskRepository.findByAssigneesContaining(userId);

        Set<Task> allTasks = new HashSet<>();
        allTasks.addAll(assignedTasks);
        allTasks.addAll(assigneesTasks);

        return allTasks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getTasksByStatus(String companyId, TaskStatus status) {
        return taskRepository.findByCompanyIdAndStatusAndDeletedAtIsNull(companyId, status)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getTasksByPriority(String companyId, TaskPriority priority) {
        return taskRepository.findByCompanyIdAndPriorityAndDeletedAtIsNull(companyId, priority)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getTasksByType(String companyId, TaskType type) {
        return taskRepository.findByCompanyIdAndTypeAndDeletedAtIsNull(companyId, type)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public SubtaskDto getSubtasks(String parentTaskId) {
        List<Task> subtasks = taskRepository.findByParentTaskIdAndDeletedAtIsNull(parentTaskId);
        Long completedCount = taskRepository.countByParentTaskIdAndStatusAndDeletedAtIsNull(
                parentTaskId,
                TaskStatus.COMPLETED
        );

        return SubtaskDto.builder()
                .parentTaskId(parentTaskId)
                .subtasks(subtasks.stream().map(this::toDto).collect(Collectors.toList()))
                .totalSubtask(subtasks.size())
                .completedSubtasks(completedCount.intValue())
                .build();
    }

    public List<TaskDto> getTasksByAsset(String assetId) {
        return taskRepository.findByAssetIdAndDeletedAtIsNull(assetId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getTasksByEquipment(String equipmentId) {
        return taskRepository.findByEquipmentIdAndDeletedAtIsNull(equipmentId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getTasksByLocation(String locationId) {
        return taskRepository.findByLocationIdAndDeletedAtIsNull(locationId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getOverdueTasks(String companyId) {
        List<TaskStatus> excludedStatuses = Arrays.asList(
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
        );

        return taskRepository.findByCompanyIdAndDueDateBeforeAndStatusNotInAndDeletedAtIsNull(
                        companyId,
                        LocalDateTime.now(),
                        excludedStatuses
                )
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getRecurringTasks(String companyId) {
        return taskRepository.findByIsRecurringAndDeletedAtIsNull(true)
                .stream()
                .filter(task -> task.getCompanyId().equals(companyId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getTaskTemplates(String companyId) {
        return taskRepository.findByIsTemplateAndDeletedAtIsNull(true)
                .stream()
                .filter(task -> task.getCompanyId().equals(companyId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> searchTasks(TaskSearchRequest request) {
        List<Task> tasks = taskRepository.findByCompanyIdAndDeletedAtIsNull(request.getCompanyId());

        return tasks.stream()
                .filter(task -> matchesSearchCriteria(task, request))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskDto updateTask(String taskId, TaskUpdateRequest request) {
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            TaskStatus oldStatus = task.getStatus();
            task.setStatus(request.getStatus());

            // Auto-complete task
            if (request.getStatus() == TaskStatus.COMPLETED && task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
                task.setProgressPercentage(100);
            }

            taskEventProducer.publishTaskStatusChangedEvent(task, oldStatus);
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getType() != null) {
            task.setType(request.getType());
        }
        if (request.getCategory() != null) {
            task.setCategory(request.getCategory());
        }
        if (request.getAssignedTo() != null) {
            task.setAssignedTo(request.getAssignedTo());
        }
        if (request.getAssignees() != null) {
            task.setAssignees(request.getAssignees());
        }
        if (request.getStartDate() != null) {
            task.setStartDate(request.getStartDate());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getActualHours() != null) {
            task.setActualHours(request.getActualHours());
        }
        if (request.getProgressPercentage() != null) {
            task.setProgressPercentage(request.getProgressPercentage());
        }
        if (request.getParentTaskId() != null) {
            task.setParentTaskId(request.getParentTaskId());
        }
        if (request.getDependsOn() != null) {
            task.setDependsOn(request.getDependsOn());
        }
        if (request.getAssetId() != null) {
            task.setAssetId(request.getAssetId());
        }
        if (request.getEquipmentId() != null) {
            task.setEquipmentId(request.getEquipmentId());
        }
        if (request.getLocationId() != null) {
            task.setLocationId(request.getLocationId());
        }
        if (request.getAttachmentIds() != null) {
            task.setAttachmentIds(request.getAttachmentIds());
        }
        if (request.getTags() != null) {
            task.setTags(request.getTags());
        }
        if (request.getIsRecurring() != null) {
            task.setIsRecurring(request.getIsRecurring());
        }
        if (request.getRecurrencePattern() != null) {
            task.setRecurrencePattern(request.getRecurrencePattern());
        }

        task = taskRepository.save(task);

        taskEventProducer.publishTaskUpdatedEvent(task);

        return toDto(task);
    }

    @Transactional
    public TaskDto assignTask(String taskId, TaskAssignRequest request) {
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        List<String> previousAssignees = task.getAssignees();
        task.setAssignees(request.getAssignees());

        if (!request.getAssignees().isEmpty()) {
            task.setAssignedTo(request.getAssignees().get(0));
        }

        if (task.getStatus() == TaskStatus.TODO) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }

        task = taskRepository.save(task);

        taskEventProducer.publishTaskAssignedEvent(task, previousAssignees);

        return toDto(task);
    }

    @Transactional
    public TaskDto updateProgress(String taskId, TaskProgressUpdateRequest request) {
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setProgressPercentage(request.getProgressPercentage());

        if (request.getActualHours() != null) {
            task.setActualHours(request.getActualHours());
        }

        if (request.getProgressPercentage() == 100 && task.getStatus() != TaskStatus.COMPLETED) {
            task.setStatus(TaskStatus.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());
        }

        task = taskRepository.save(task);

        taskEventProducer.publishTaskProgressUpdatedEvent(task);

        return toDto(task);
    }

    @Transactional
    public TaskDto completeTask(String taskId) {
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        task.setProgressPercentage(100);

        task = taskRepository.save(task);

        taskEventProducer.publishTaskCompletedEvent(task);

        return toDto(task);
    }

    @Transactional
    public void deleteTask(String taskId, boolean hardDelete) {
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (hardDelete) {
            taskRepository.delete(task);
        } else {
            task.setDeletedAt(LocalDateTime.now());
            taskRepository.save(task);
        }

        taskEventProducer.publishTaskDeletedEvent(task, hardDelete);
    }

    public TaskStatsResponse getTaskStats(String companyId) {
        Long totalTasks = taskRepository.countByCompanyIdAndDeletedAtIsNull(companyId);
        Long todoTasks = taskRepository.countByCompanyIdAndStatusAndDeletedAtIsNull(companyId, TaskStatus.TODO);
        Long inProgressTasks = taskRepository.countByCompanyIdAndStatusAndDeletedAtIsNull(companyId, TaskStatus.IN_PROGRESS);
        Long completedTasks = taskRepository.countByCompanyIdAndStatusAndDeletedAtIsNull(companyId, TaskStatus.COMPLETED);
        Long overdueCount = (long) getOverdueTasks(companyId).size();

        Map<String, Long> tasksByStatus = convertToMap(taskRepository.countByStatusGrouped(companyId));
        Map<String, Long> tasksByPriority = convertToMap(taskRepository.countByPriorityGrouped(companyId));
        Map<String, Long> tasksByType = convertToMap(taskRepository.countByTypeGrouped(companyId));
        Map<String, Long> tasksByCategory = convertToMap(taskRepository.countByCategoryGrouped(companyId));

        Double completionRate = taskRepository.getCompletionRate(companyId);
        Integer totalEstimatedHours = taskRepository.getTotalEstimatedHours(companyId);
        Integer totalActualHours = taskRepository.getTotalActualHours(companyId);

        return TaskStatsResponse.builder()
                .totalTasks(totalTasks)
                .todoTasks(todoTasks)
                .inProgressTasks(inProgressTasks)
                .completedTasks(completedTasks)
                .overdueTasks(overdueCount)
                .tasksByStatus(tasksByStatus)
                .tasksByPriority(tasksByPriority)
                .tasksByType(tasksByType)
                .tasksByCategory(tasksByCategory)
                .build();
    }


    private boolean matchesSearchCriteria(Task task, TaskSearchRequest request) {
        if (request.getKeyword() != null &&
                !task.getTitle().toLowerCase().contains(request.getKeyword().toLowerCase()) &&
                (task.getDescription() == null || !task.getDescription().toLowerCase().contains(request.getKeyword().toLowerCase()))) {
            return false;
        }
        if (request.getProjectId() != null && !request.getProjectId().equals(task.getProjectId())) {
            return false;
        }
        if (request.getCreatedBy() != null && !request.getCreatedBy().equals(task.getCreatedBy())) {
            return false;
        }
        if (request.getAssignedTo() != null && !request.getAssignedTo().equals(task.getAssignedTo())) {
            return false;
        }
        if (request.getStatus() != null && request.getStatus() != task.getStatus()) {
            return false;
        }
        if (request.getPriority() != null && request.getPriority() != task.getPriority()) {
            return false;
        }
        if (request.getType() != null && request.getType() != task.getType()) {
            return false;
        }
        if (request.getCategory() != null && request.getCategory() != task.getCategory()) {
            return false;
        }
        if (request.getParentTaskId() != null && !request.getParentTaskId().equals(task.getParentTaskId())) {
            return false;
        }
        if (request.getAssetId() != null && !request.getAssetId().equals(task.getAssetId())) {
            return false;
        }
        if (request.getEquipmentId() != null && !request.getEquipmentId().equals(task.getEquipmentId())) {
            return false;
        }
        if (request.getLocationId() != null && !request.getLocationId().equals(task.getLocationId())) {
            return false;
        }
        if (request.getIsRecurring() != null && !request.getIsRecurring().equals(task.getIsRecurring())) {
            return false;
        }

        if (request.getCreatedAfter() != null && task.getCreatedAt().isBefore(request.getCreatedAfter())) {
            return false;
        }
        if (request.getCreatedBefore() != null && task.getCreatedAt().isAfter(request.getCreatedBefore())) {
            return false;
        }
        if (request.getDueDateAfter() != null && (task.getDueDate() == null || task.getDueDate().isBefore(request.getDueDateAfter()))) {
            return false;
        }
        if (request.getDueDateBefore() != null && (task.getDueDate() == null || task.getDueDate().isAfter(request.getDueDateBefore()))) {
            return false;
        }
        if (request.getMinProgressPercentage() != null && (task.getProgressPercentage() == null || task.getProgressPercentage() < request.getMinProgressPercentage())) {
            return false;
        }
        if (request.getMaxProgressPercentage() != null && (task.getProgressPercentage() == null || task.getProgressPercentage() > request.getMaxProgressPercentage())) {
            return false;
        }
        return true;
    }

    private Map<String, Long> convertToMap(List<Object[]> results) {
        return results.stream()
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> (Long) arr[1]
                ));
    }

    private TaskDto toDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .companyId(task.getCompanyId())
                .projectId(task.getProjectId())
                .createdBy(task.getCreatedBy())
                .assignedTo(task.getAssignedTo())
                .assignees(task.getAssignees())
                .status(task.getStatus())
                .priority(task.getPriority())
                .type(task.getType())
                .category(task.getCategory())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .progressPercentage(task.getProgressPercentage())
                .parentTaskId(task.getParentTaskId())
                .dependsOn(task.getDependsOn())
                .assetId(task.getAssetId())
                .equipmentId(task.getEquipmentId())
                .locationId(task.getLocationId())
                .attachmentIds(task.getAttachmentIds())
                .tags(task.getTags())
                .isRecurring(task.getIsRecurring())
                .recurrencePattern(task.getRecurrencePattern())

                .build();
    }


}
