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
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") EnhancedDocumentUploadRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        log.info("Uploading progress document for task: {}", request.getTaskId());
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