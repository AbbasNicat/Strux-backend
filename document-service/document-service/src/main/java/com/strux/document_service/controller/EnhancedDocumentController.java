package com.strux.document_service.controller;

import com.strux.document_service.dto.*;
import com.strux.document_service.enums.*;
import com.strux.document_service.service.EnhancedDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class EnhancedDocumentController {

    private final EnhancedDocumentService documentService;

    @PostMapping(value = "/progress/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDto> uploadProgressDocument(
            @RequestPart("file") MultipartFile file,  // ✅ @RequestPart
            @RequestParam("companyId") String companyId,
            @RequestParam("entityType") String entityType,  // ✅ String kimi qəbul et
            @RequestParam("entityId") String entityId,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "taskId", required = false) String taskId,
            @RequestParam(value = "phaseId", required = false) String phaseId,
            @RequestParam(value = "workerId", required = false) String workerId,
            @RequestParam(value = "comments", required = false) String comments,
            @RequestParam(value = "comparisonType", required = false) String comparisonType,
            @RequestParam(value = "completionPercentage", required = false) Integer completionPercentage,
            @RequestParam(value = "requiresApproval", required = false) Boolean requiresApproval,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        log.info("📥 Uploading progress document");
        log.info("📥 File: {}", file.getOriginalFilename());
        log.info("📥 File size: {}", file.getSize());
        log.info("📥 Content type: {}", file.getContentType());
        log.info("📥 Company ID: {}", companyId);
        log.info("📥 Entity Type: {}", entityType);
        log.info("📥 Task ID: {}", taskId);
        log.info("📥 User ID: {}", userId);

        // Enum conversion
        EntityType entityTypeEnum = EntityType.valueOf(entityType);
        DocumentType docTypeEnum = documentType != null ? DocumentType.valueOf(documentType) : null;
        DocumentCategory categoryEnum = category != null ? DocumentCategory.valueOf(category) : null;

        // Build request object
        EnhancedDocumentUploadRequest request = new EnhancedDocumentUploadRequest();
        request.setCompanyId(companyId);
        request.setEntityType(entityTypeEnum);
        request.setEntityId(entityId);
        request.setDocumentType(docTypeEnum);
        request.setCategory(categoryEnum);
        request.setDescription(description);
        request.setTaskId(taskId);
        request.setPhaseId(phaseId);
        request.setWorkerId(workerId);
        request.setComments(comments);
        request.setComparisonType(comparisonType);
        request.setCompletionPercentage(completionPercentage);
        request.setRequiresApproval(requiresApproval);

        DocumentDto document = documentService.uploadProgressDocument(file, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }

    /**
     * Get timeline of documents for a unit
     */
    @GetMapping("/timeline/unit/{unitId}")
    public ResponseEntity<List<TimelineDocumentDto>> getUnitTimeline(
            @PathVariable String unitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        List<TimelineDocumentDto> timeline = documentService.getUnitTimeline(unitId, startDate, endDate);
        return ResponseEntity.ok(timeline);
    }


    @GetMapping("/task/{taskId}/comparison")
    public ResponseEntity<Map<String, List<DocumentDto>>> getTaskComparison(@PathVariable String taskId) {
        Map<String, List<DocumentDto>> comparison = documentService.getBeforeAfterPhotos(taskId);
        return ResponseEntity.ok(comparison);
    }


    @GetMapping("/phase/{phaseId}/category/{category}")
    public ResponseEntity<List<DocumentDto>> getPhaseDocuments(
            @PathVariable String phaseId,
            @PathVariable DocumentCategory category
    ) {
        List<DocumentDto> documents = documentService.getDocumentsByPhaseAndCategory(phaseId, category);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/pending-approval")
    public ResponseEntity<List<DocumentDto>> getPendingApprovals(
            @RequestParam(required = false) String companyId
    ) {
        List<DocumentDto> documents = documentService.getPendingApprovals(companyId);
        return ResponseEntity.ok(documents);
    }

    @PutMapping("/{documentId}/approve")
    public ResponseEntity<DocumentDto> approveDocument(
            @PathVariable String documentId,
            @RequestBody DocumentApprovalRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        log.info("Document {} being reviewed by {}", documentId, userId);
        DocumentDto document = documentService.approveDocument(documentId, request, userId);
        return ResponseEntity.ok(document);
    }

    // === WORKER SPECIFIC ===

    @GetMapping("/worker/{workerId}")
    public ResponseEntity<List<DocumentDto>> getWorkerDocuments(
            @PathVariable String workerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate
    ) {
        List<DocumentDto> documents = documentService.getDocumentsByWorker(workerId, startDate);
        return ResponseEntity.ok(documents);
    }

    // === HOMEOWNER VIEW ===

    @GetMapping("/homeowner/units")
    public ResponseEntity<Map<String, List<DocumentDto>>> getHomeownerDocuments(
            @RequestParam List<String> unitIds
    ) {
        Map<String, List<DocumentDto>> documentsByUnit = documentService.getDocumentsByUnits(unitIds);
        return ResponseEntity.ok(documentsByUnit);
    }


    @GetMapping("/project/{projectId}/stats")
    public ResponseEntity<Map<String, Object>> getProjectDocumentStats(@PathVariable String projectId) {
        Map<String, Object> stats = documentService.getProjectStats(projectId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/project/{projectId}/progress")
    public ResponseEntity<Map<String, Object>> getProjectProgress(@PathVariable String projectId) {
        Map<String, Object> progress = documentService.calculateProjectProgress(projectId);
        return ResponseEntity.ok(progress);
    }
}