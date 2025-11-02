package com.strux.unit_service.repository;

import com.strux.unit_service.enums.WorkItemStatus;
import com.strux.unit_service.model.UnitWorkItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UnitWorkItemRepository extends JpaRepository<UnitWorkItem, String> {

    List<UnitWorkItem> findByUnitId(String unitId);

    List<UnitWorkItem> findByUnitIdOrderByCreatedAtAsc(String unitId);

    List<UnitWorkItem> findByUnitIdOrderByWeightPercentageDesc(String unitId);

    List<UnitWorkItem> findByStatus(WorkItemStatus status);

    List<UnitWorkItem> findByUnitIdAndStatus(String unitId, WorkItemStatus status);

    List<UnitWorkItem> findByAssignedContractorId(String contractorId);

    List<UnitWorkItem> findByAssignedContractorIdAndStatus(String contractorId, WorkItemStatus status);

    List<UnitWorkItem> findByAssignedWorkerId(String workerId);

    List<UnitWorkItem> findByAssignedWorkerIdAndStatus(String workerId, WorkItemStatus status);

    Optional<UnitWorkItem> findByTaskId(String taskId);

    List<UnitWorkItem> findByTaskIdIsNotNull();

    boolean existsByTaskId(String taskId);

    List<UnitWorkItem> findByCompletionPercentageGreaterThanEqual(Integer minPercentage);

    List<UnitWorkItem> findByCompletionPercentageLessThan(Integer maxPercentage);

    List<UnitWorkItem> findByUnitIdAndCompletionPercentageLessThan(String unitId, Integer maxPercentage);

    @Query("SELECT w FROM UnitWorkItem w WHERE w.dueDate < :now AND w.status NOT IN ('COMPLETED', 'VERIFIED', 'FAILED')")
    List<UnitWorkItem> findOverdueWorkItems(@Param("now") LocalDateTime now);

    @Query("SELECT w FROM UnitWorkItem w WHERE w.unitId = :unitId AND w.dueDate < :now AND w.status NOT IN ('COMPLETED', 'VERIFIED', 'FAILED')")
    List<UnitWorkItem> findOverdueWorkItemsByUnit(@Param("unitId") String unitId, @Param("now") LocalDateTime now);

    List<UnitWorkItem> findByStartDateBetween(LocalDateTime start, LocalDateTime end);

    List<UnitWorkItem> findByDueDateBetween(LocalDateTime start, LocalDateTime end);

    List<UnitWorkItem> findByUnitIdAndStartDateBetween(String unitId, LocalDateTime start, LocalDateTime end);

    List<UnitWorkItem> findByCompletedAtIsNotNull();

    List<UnitWorkItem> findByUnitIdAndCompletedAtIsNotNull(String unitId);

    List<UnitWorkItem> findByUnitIdAndCompletedAtBetween(String unitId, LocalDateTime start, LocalDateTime end);

    Long countByUnitId(String unitId);

    Long countByUnitIdAndStatus(String unitId, WorkItemStatus status);

    Long countByAssignedContractorId(String contractorId);

    Long countByAssignedWorkerId(String workerId);

    Long countByStatus(WorkItemStatus status);

    @Query("SELECT w.status, COUNT(w) FROM UnitWorkItem w WHERE w.unitId = :unitId GROUP BY w.status")
    List<Object[]> countByStatusGrouped(@Param("unitId") String unitId);

    @Query("SELECT AVG(w.completionPercentage) FROM UnitWorkItem w WHERE w.unitId = :unitId")
    Double getAverageCompletionPercentage(@Param("unitId") String unitId);

    @Query("SELECT SUM(w.weightPercentage) FROM UnitWorkItem w WHERE w.unitId = :unitId")
    Integer getTotalWeightPercentage(@Param("unitId") String unitId);

    @Query("SELECT SUM(w.weightPercentage) FROM UnitWorkItem w WHERE w.unitId = :unitId AND w.status = 'COMPLETED'")
    Integer getCompletedWeightPercentage(@Param("unitId") String unitId);

    @Query("SELECT w FROM UnitWorkItem w WHERE w.unitId = :unitId AND LOWER(w.workName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<UnitWorkItem> searchByWorkName(@Param("unitId") String unitId, @Param("keyword") String keyword);

    @Query("SELECT w FROM UnitWorkItem w WHERE w.unitId = :unitId AND w.status = 'IN_PROGRESS' AND w.completionPercentage > 0")
    List<UnitWorkItem> findActiveWorkItems(@Param("unitId") String unitId);

    @Modifying
    @Query("DELETE FROM UnitWorkItem w WHERE w.unitId = :unitId")
    void deleteByUnitId(@Param("unitId") String unitId);

    boolean existsByUnitIdAndWorkName(String unitId, String workName);
}
