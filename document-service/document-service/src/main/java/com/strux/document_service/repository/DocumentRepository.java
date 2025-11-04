package com.strux.document_service.repository;

import com.strux.document_service.enums.DocumentStatus;
import com.strux.document_service.model.Document;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.strux.document_service.enums.DocumentApprovalStatus;
import com.strux.document_service.enums.DocumentCategory;
import com.strux.document_service.enums.DocumentStatus;
import com.strux.document_service.enums.EntityType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentRepository extends MongoRepository<Document, String> {

    // ==================== EXISTING METHODS ====================

    List<Document> findByEntityTypeAndEntityIdAndStatus(EntityType entityType, String entityId, DocumentStatus status);

    List<Document> findByCompanyIdAndStatus(String companyId, DocumentStatus status);

    Long countByCompanyIdAndStatus(String companyId, DocumentStatus status);

    @Aggregation(pipeline = {
            "{ '$match': { 'companyId': ?0, 'status': 'ACTIVE' } }",
            "{ '$group': { '_id': null, 'totalSize': { '$sum': '$fileSize' } } }"
    })
    Long getTotalStorageByCompany(String companyId);

    List<Document> findByEntityIdAndEntityType(String entityId, EntityType entityType);

    /**
     * Find documents by task ID
     */
    List<Document> findByTaskId(String taskId);

    /**
     * Find documents by phase ID and category
     */
    List<Document> findByPhaseIdAndCategory(String phaseId, DocumentCategory category);

    /**
     * Find documents by worker ID
     */
    List<Document> findByWorkerId(String workerId);

    /**
     * Find documents by approval status
     */
    List<Document> findByApprovalStatus(DocumentApprovalStatus approvalStatus);

    /**
     * Find documents by company ID and approval status
     */
    List<Document> findByCompanyIdAndApprovalStatus(String companyId, DocumentApprovalStatus approvalStatus);

    /**
     * Find documents by entity type, entity ID and upload date range
     */
    List<Document> findByEntityIdAndEntityTypeAndUploadedAtBetween(
            String entityId,
            EntityType entityType,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * Find documents by worker and date range
     */
    List<Document> findByWorkerIdAndUploadedAtAfter(String workerId, LocalDateTime startDate);

    /**
     * Find documents by task ID and comparison type
     */
    List<Document> findByTaskIdAndComparisonType(String taskId, String comparisonType);

    /**
     * Find documents by company, status and approval status
     */
    List<Document> findByCompanyIdAndStatusAndApprovalStatus(
            String companyId,
            DocumentStatus status,
            DocumentApprovalStatus approvalStatus
    );


    List<Document> findByPhaseId(String phaseId);

    Long countByTaskId(String taskId);

    /**
     * Count documents by worker ID
     */
    Long countByWorkerId(String workerId);

    /**
     * Find recent documents for a project
     */
    @Query("{ 'entityId': ?0, 'entityType': 'PROJECT', 'uploadedAt': { $gte: ?1 } }")
    List<Document> findRecentProjectDocuments(String projectId, LocalDateTime since);

    /**
     * Find documents requiring approval for a company
     */
    @Query("{ 'companyId': ?0, 'approvalStatus': 'PENDING_REVIEW' }")
    List<Document> findPendingApprovalsForCompany(String companyId);

    /**
     * Get total storage by project
     */
    @Query(value = "{ 'entityId': ?0, 'entityType': 'PROJECT', 'status': 'ACTIVE' }",
            fields = "{ 'fileSize': 1 }")
    List<Document> findProjectDocumentsForStorage(String projectId);
}
