package com.strux.task_service.repository;

import com.strux.task_service.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import com.strux.task_service.enums.*;
import com.strux.task_service.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    List<Task> findByCompanyIdAndDeletedAtIsNull(String companyId);

    List<Task> findByProjectIdAndDeletedAtIsNull(String projectId);

    List<Task> findByCreatedByAndDeletedAtIsNull(String createdBy);

    List<Task> findByAssignedToAndDeletedAtIsNull(String assignedTo);

    @Query("SELECT t FROM Task t WHERE :userId MEMBER OF t.assignees AND t.deletedAt IS NULL")
    List<Task> findByAssigneesContaining(@Param("userId") String userId);

    List<Task> findByCompanyIdAndStatusAndDeletedAtIsNull(String companyId, TaskStatus status);

    List<Task> findByCompanyIdAndPriorityAndDeletedAtIsNull(String companyId, TaskPriority priority);

    List<Task> findByCompanyIdAndTypeAndDeletedAtIsNull(String companyId, TaskType type);

    List<Task> findByParentTaskIdAndDeletedAtIsNull(String parentTaskId);

    List<Task> findByAssetIdAndDeletedAtIsNull(String assetId);

    List<Task> findByEquipmentIdAndDeletedAtIsNull(String equipmentId);

    List<Task> findByLocationIdAndDeletedAtIsNull(String locationId);

    @Query("SELECT t FROM Task t WHERE :tag MEMBER OF t.tags AND t.deletedAt IS NULL")
    List<Task> findByTag(@Param("tag") String tag);

    List<Task> findByIsRecurringAndDeletedAtIsNull(Boolean isRecurring);

    List<Task> findByIsTemplateAndDeletedAtIsNull(Boolean isTemplate);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :now AND t.status NOT IN ('COMPLETED', 'CANCELLED') AND t.deletedAt IS NULL")
    List<Task> findOverdueTasks(@Param("now") LocalDateTime now);

    List<Task> findByCompanyIdAndDueDateBeforeAndStatusNotInAndDeletedAtIsNull(
            String companyId,
            LocalDateTime dueDate,
            List<TaskStatus> statuses
    );


    @Query("SELECT t FROM Task t WHERE (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND t.deletedAt IS NULL")
    List<Task> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT t FROM Task t WHERE t.companyId = :companyId AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND t.deletedAt IS NULL")
    List<Task> searchByKeywordAndCompany(@Param("keyword") String keyword, @Param("companyId") String companyId);


    Long countByCompanyIdAndDeletedAtIsNull(String companyId);

    Long countByCompanyIdAndStatusAndDeletedAtIsNull(String companyId, TaskStatus status);

    Long countByParentTaskIdAndStatusAndDeletedAtIsNull(String parentTaskId, TaskStatus status);

    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.companyId = :companyId AND t.deletedAt IS NULL GROUP BY t.status")
    List<Object[]> countByStatusGrouped(@Param("companyId") String companyId);

    @Query("SELECT t.priority, COUNT(t) FROM Task t WHERE t.companyId = :companyId AND t.deletedAt IS NULL GROUP BY t.priority")
    List<Object[]> countByPriorityGrouped(@Param("companyId") String companyId);

    @Query("SELECT t.type, COUNT(t) FROM Task t WHERE t.companyId = :companyId AND t.deletedAt IS NULL GROUP BY t.type")
    List<Object[]> countByTypeGrouped(@Param("companyId") String companyId);

    @Query("SELECT t.category, COUNT(t) FROM Task t WHERE t.companyId = :companyId AND t.deletedAt IS NULL GROUP BY t.category")
    List<Object[]> countByCategoryGrouped(@Param("companyId") String companyId);

    @Query("SELECT (COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(t)) FROM Task t WHERE t.companyId = :companyId AND t.deletedAt IS NULL")
    Double getCompletionRate(@Param("companyId") String companyId);

    @Query("SELECT SUM(t.estimatedHours) FROM Task t WHERE t.companyId = :companyId AND t.deletedAt IS NULL")
    Integer getTotalEstimatedHours(@Param("companyId") String companyId);

    @Query("SELECT SUM(t.actualHours) FROM Task t WHERE t.companyId = :companyId AND t.deletedAt IS NULL")
    Integer getTotalActualHours(@Param("companyId") String companyId);

}
