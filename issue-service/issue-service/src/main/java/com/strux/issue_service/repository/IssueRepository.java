package com.strux.issue_service.repository;

import com.strux.issue_service.enums.*;
import com.strux.issue_service.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<Issue, String> {

    // Find by company
    List<Issue> findByCompanyIdAndDeletedAtIsNull(String companyId);

    // Find by user (created by)
    List<Issue> findByUserIdAndDeletedAtIsNull(String userId);

    // Find by assigned user
    List<Issue> findByAssignedToAndDeletedAtIsNull(String assignedTo);

    // Find by status
    List<Issue> findByStatusAndDeletedAtIsNull(IssueStatus status);

    List<Issue> findByCompanyIdAndStatusAndDeletedAtIsNull(String companyId, IssueStatus status);

    // Find by category
    List<Issue> findByCategoryAndDeletedAtIsNull(IssueCategory category);

    List<Issue> findByCompanyIdAndCategoryAndDeletedAtIsNull(String companyId, IssueCategory category);

    // Find by type
    List<Issue> findByTypeAndDeletedAtIsNull(IssueType type);

    List<Issue> findByCompanyIdAndTypeAndDeletedAtIsNull(String companyId, IssueType type);

    // Find by related entities
    List<Issue> findByProjectIdAndDeletedAtIsNull(String projectId);

    List<Issue> findByTaskIdAndDeletedAtIsNull(String taskId);

    List<Issue> findByAssetIdAndDeletedAtIsNull(String assetId);

    // Find overdue issues
    @Query("SELECT i FROM Issue i WHERE i.dueDate < :now AND i.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED') AND i.deletedAt IS NULL")
    List<Issue> findOverdueIssues(@Param("now") LocalDateTime now);

    List<Issue> findByCompanyIdAndDueDateBeforeAndStatusNotInAndDeletedAtIsNull(
            String companyId,
            LocalDateTime dueDate,
            List<IssueStatus> statuses
    );

    // Search by title or description
    @Query("SELECT i FROM Issue i WHERE (LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND i.deletedAt IS NULL")
    List<Issue> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT i FROM Issue i WHERE i.companyId = :companyId AND (LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND i.deletedAt IS NULL")
    List<Issue> searchByKeywordAndCompany(@Param("keyword") String keyword, @Param("companyId") String companyId);

    // Find by date range
    List<Issue> findByCreatedAtBetweenAndDeletedAtIsNull(LocalDateTime start, LocalDateTime end);

    List<Issue> findByCompanyIdAndCreatedAtBetweenAndDeletedAtIsNull(String companyId, LocalDateTime start, LocalDateTime end);

    // Count queries
    Long countByCompanyIdAndStatusAndDeletedAtIsNull(String companyId, IssueStatus status);


    Long countByCompanyIdAndCategoryAndDeletedAtIsNull(String companyId, IssueCategory category);

    Long countByCompanyIdAndTypeAndDeletedAtIsNull(String companyId, IssueType type);

    Long countByCompanyIdAndDeletedAtIsNull(String companyId);

    Long countByAssignedToAndStatusAndDeletedAtIsNull(String assignedTo, IssueStatus status);

    // Statistics queries
    @Query("SELECT i.status, COUNT(i) FROM Issue i WHERE i.companyId = :companyId AND i.deletedAt IS NULL GROUP BY i.status")
    List<Object[]> countByStatusGrouped(@Param("companyId") String companyId);

    @Query("SELECT i.category, COUNT(i) FROM Issue i WHERE i.companyId = :companyId AND i.deletedAt IS NULL GROUP BY i.category")
    List<Object[]> countByCategoryGrouped(@Param("companyId") String companyId);

    @Query("SELECT i.type, COUNT(i) FROM Issue i WHERE i.companyId = :companyId AND i.deletedAt IS NULL GROUP BY i.type")
    List<Object[]> countByTypeGrouped(@Param("companyId") String companyId);

    // Check if exists
    boolean existsByIdAndDeletedAtIsNull(String id);
}
