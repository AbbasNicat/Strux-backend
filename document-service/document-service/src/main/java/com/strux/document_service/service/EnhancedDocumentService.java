package com.strux.document_service.service;

import com.strux.document_service.dto.*;
import com.strux.document_service.enums.*;
import com.strux.document_service.event.DocumentApprovedEvent;
import com.strux.document_service.event.ProgressDocumentUploadedEvent;
import com.strux.document_service.model.Document;
import com.strux.document_service.model.Folder;
import com.strux.document_service.repository.DocumentRepository;
import com.strux.document_service.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedDocumentService {

    private final DocumentRepository documentRepository;
    private final FolderRepository folderRepository;
    private final MinioStorageService storageService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ==================== PROGRESS DOCUMENT UPLOAD ====================

    @Transactional
    public DocumentDto uploadProgressDocument(MultipartFile file, EnhancedDocumentUploadRequest request, String uploadedBy) {
        try {
            log.info("Uploading progress document for task: {}, worker: {}", request.getTaskId(), uploadedBy);

            // Validate file
            validateFile(file);

            // Folder kontrolü ve path oluşturma
            String folder;
            if (request.getFolderId() != null) {
                Folder targetFolder = folderRepository.findByIdAndIsDeletedFalse(request.getFolderId())
                        .orElseThrow(() -> new RuntimeException("Folder not found"));

                // MinIO path: folder'ın path'i + task bilgisi
                folder = targetFolder.getFolderPath().substring(1);
                if (request.getTaskId() != null) {
                    folder += "/task-" + request.getTaskId();
                }
                folder += "/" + (request.getCategory() != null ? request.getCategory().name().toLowerCase() : "general");
            } else {
                // Eski sistem
                folder = generateEnhancedFolderPath(request);
            }

            // Upload to MinIO
            String filePath = storageService.uploadFile(file, folder);

            // Create document entity
            Document document = Document.builder()
                    .fileName(extractFileName(filePath))
                    .originalFileName(file.getOriginalFilename())
                    .filePath(filePath)
                    .bucketName("strux-documents")
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .fileExtension(getFileExtension(file.getOriginalFilename()))
                    .documentType(request.getDocumentType())
                    .category(request.getCategory())
                    .description(request.getDescription())
                    .tags(request.getTags() != null ? request.getTags() : new HashSet<>())
                    .entityType(request.getEntityType())
                    .entityId(request.getEntityId())
                    .uploadedBy(uploadedBy)
                    .companyId(request.getCompanyId())
                    .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
                    .status(DocumentStatus.ACTIVE)
                    .folderId(request.getFolderId())
                    // Enhanced fields
                    .taskId(request.getTaskId())
                    .phaseId(request.getPhaseId())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .locationDescription(request.getLocationDescription())
                    .workerId(request.getWorkerId())
                    .comments(request.getComments())
                    .relatedDocumentId(request.getRelatedDocumentId())
                    .comparisonType(request.getComparisonType())
                    .approvalStatus(request.getRequiresApproval() != null && request.getRequiresApproval()
                            ? DocumentApprovalStatus.PENDING_REVIEW
                            : DocumentApprovalStatus.AUTO_APPROVED)
                    .build();

            document = documentRepository.save(document);
            log.info("Progress document saved with ID: {}", document.getId());

            // Publish progress event
            publishProgressDocumentEvent(document, request);

            return toDto(document);

        } catch (Exception e) {
            log.error("Error uploading progress document: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload progress document: " + e.getMessage());
        }
    }

    // ==================== TIMELINE ====================

    public List<TimelineDocumentDto> getUnitTimeline(String unitId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching timeline for unit: {}", unitId);

        List<Document> documents = documentRepository.findByEntityIdAndEntityType(unitId, EntityType.UNIT);

        // Filter by date range if provided
        if (startDate != null || endDate != null) {
            documents = documents.stream()
                    .filter(doc -> {
                        LocalDateTime uploadTime = doc.getUploadedAt();
                        if (uploadTime == null) return false;

                        boolean afterStart = startDate == null || uploadTime.isAfter(startDate);
                        boolean beforeEnd = endDate == null || uploadTime.isBefore(endDate);

                        return afterStart && beforeEnd;
                    })
                    .collect(Collectors.toList());
        }

        // Group by date and phase
        Map<LocalDateTime, Map<String, List<Document>>> groupedByDateAndPhase = documents.stream()
                .filter(doc -> doc.getUploadedAt() != null)
                .collect(Collectors.groupingBy(
                        doc -> doc.getUploadedAt().toLocalDate().atStartOfDay(),
                        Collectors.groupingBy(
                                doc -> doc.getPhaseId() != null ? doc.getPhaseId() : "general"
                        )
                ));

        // Convert to TimelineDocumentDto
        List<TimelineDocumentDto> timeline = new ArrayList<>();
        groupedByDateAndPhase.forEach((date, phaseMap) -> {
            phaseMap.forEach((phase, docs) -> {
                List<DocumentDto> docDtos = docs.stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());

                timeline.add(TimelineDocumentDto.builder()
                        .date(date)
                        .phase(phase)
                        .documents(docDtos)
                        .milestone(determineMilestone(docs))
                        .progressPercentage(calculatePhaseProgress(docs))
                        .build());
            });
        });

        // Sort by date descending
        timeline.sort(Comparator.comparing(TimelineDocumentDto::getDate).reversed());

        return timeline;
    }

    // ==================== BEFORE/AFTER COMPARISON ====================

    public Map<String, List<DocumentDto>> getBeforeAfterPhotos(String taskId) {
        log.info("Fetching before/after photos for task: {}", taskId);

        List<Document> taskDocuments = documentRepository.findByTaskId(taskId);

        Map<String, List<DocumentDto>> comparison = new HashMap<>();

        List<DocumentDto> beforePhotos = taskDocuments.stream()
                .filter(doc -> "BEFORE".equalsIgnoreCase(doc.getComparisonType()))
                .map(this::toDto)
                .collect(Collectors.toList());

        List<DocumentDto> afterPhotos = taskDocuments.stream()
                .filter(doc -> "AFTER".equalsIgnoreCase(doc.getComparisonType()))
                .map(this::toDto)
                .collect(Collectors.toList());

        List<DocumentDto> duringPhotos = taskDocuments.stream()
                .filter(doc -> "DURING".equalsIgnoreCase(doc.getComparisonType()))
                .map(this::toDto)
                .collect(Collectors.toList());

        comparison.put("before", beforePhotos);
        comparison.put("after", afterPhotos);
        comparison.put("during", duringPhotos);

        return comparison;
    }

    // ==================== PHASE DOCUMENTS ====================

    public List<DocumentDto> getDocumentsByPhaseAndCategory(String phaseId, DocumentCategory category) {
        log.info("Fetching documents for phase: {}, category: {}", phaseId, category);

        return documentRepository.findByPhaseIdAndCategory(phaseId, category)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Map<String, List<DocumentDto>> getDocumentsByPhaseGroupedByFolder(String phaseId) {
        log.info("Fetching documents for phase: {} grouped by folder", phaseId);

        List<Document> phaseDocuments = documentRepository.findByPhaseId(phaseId);

        return phaseDocuments.stream()
                .collect(Collectors.groupingBy(
                        doc -> doc.getFolderId() != null ? doc.getFolderId() : "root",
                        Collectors.mapping(this::toDto, Collectors.toList())
                ));
    }

    // ==================== APPROVAL WORKFLOW ====================

    public List<DocumentDto> getPendingApprovals(String companyId) {
        log.info("Fetching pending approvals for company: {}", companyId);

        List<Document> documents;

        if (companyId != null) {
            documents = documentRepository.findByCompanyIdAndApprovalStatus(
                    companyId,
                    DocumentApprovalStatus.PENDING_REVIEW
            );
        } else {
            documents = documentRepository.findByApprovalStatus(DocumentApprovalStatus.PENDING_REVIEW);
        }

        return documents.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DocumentDto approveDocument(String documentId, DocumentApprovalRequest request, String approvedBy) {
        log.info("Document {} being approved/rejected by {}", documentId, approvedBy);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));

        document.setApprovalStatus(request.getStatus());
        document.setApprovedBy(approvedBy);
        document.setApproverComments(request.getApproverComments());
        document.setApprovedAt(LocalDateTime.now());
        document.setQualityRating(request.getQualityRating());

        document = documentRepository.save(document);

        // Publish approval event
        publishDocumentApprovedEvent(document, request);

        return toDto(document);
    }

    // ==================== WORKER DOCUMENTS ====================

    public List<DocumentDto> getDocumentsByWorker(String workerId, LocalDateTime startDate) {
        log.info("Fetching documents for worker: {}", workerId);

        List<Document> documents = documentRepository.findByWorkerId(workerId);

        if (startDate != null) {
            documents = documents.stream()
                    .filter(doc -> doc.getUploadedAt() != null && doc.getUploadedAt().isAfter(startDate))
                    .collect(Collectors.toList());
        }

        return documents.stream()
                .map(this::toDto)
                .sorted(Comparator.comparing(DocumentDto::getUploadedAt).reversed())
                .collect(Collectors.toList());
    }

    // ==================== HOMEOWNER DOCUMENTS ====================

    public Map<String, List<DocumentDto>> getDocumentsByUnits(List<String> unitIds) {
        log.info("Fetching documents for {} units", unitIds.size());

        Map<String, List<DocumentDto>> documentsByUnit = new HashMap<>();

        for (String unitId : unitIds) {
            List<Document> documents = documentRepository.findByEntityIdAndEntityType(
                    unitId,
                    EntityType.UNIT
            );

            List<DocumentDto> docDtos = documents.stream()
                    .filter(doc -> doc.getStatus() == DocumentStatus.ACTIVE)
                    .filter(doc -> doc.getApprovalStatus() == DocumentApprovalStatus.APPROVED
                            || doc.getApprovalStatus() == DocumentApprovalStatus.AUTO_APPROVED)
                    .map(this::toDto)
                    .collect(Collectors.toList());

            documentsByUnit.put(unitId, docDtos);
        }

        return documentsByUnit;
    }

    // ==================== STATISTICS & ANALYTICS ====================

    public Map<String, Object> getProjectStats(String projectId) {
        log.info("Calculating document statistics for project: {}", projectId);

        List<Document> projectDocuments = documentRepository.findByEntityIdAndEntityType(
                projectId,
                EntityType.PROJECT
        );

        Map<String, Object> stats = new HashMap<>();

        // Total documents
        stats.put("totalDocuments", projectDocuments.size());

        // By type
        Map<DocumentType, Long> byType = projectDocuments.stream()
                .collect(Collectors.groupingBy(Document::getDocumentType, Collectors.counting()));
        stats.put("documentsByType", byType);

        // By category
        Map<DocumentCategory, Long> byCategory = projectDocuments.stream()
                .filter(doc -> doc.getCategory() != null)
                .collect(Collectors.groupingBy(Document::getCategory, Collectors.counting()));
        stats.put("documentsByCategory", byCategory);

        // By folder
        Map<String, Long> byFolder = projectDocuments.stream()
                .filter(doc -> doc.getFolderId() != null)
                .collect(Collectors.groupingBy(Document::getFolderId, Collectors.counting()));
        stats.put("documentsByFolder", byFolder);

        // Pending approvals
        long pendingApprovals = projectDocuments.stream()
                .filter(doc -> doc.getApprovalStatus() == DocumentApprovalStatus.PENDING_REVIEW)
                .count();
        stats.put("pendingApprovals", pendingApprovals);

        // Approved
        long approved = projectDocuments.stream()
                .filter(doc -> doc.getApprovalStatus() == DocumentApprovalStatus.APPROVED)
                .count();
        stats.put("approvedDocuments", approved);

        // Total storage
        long totalStorage = projectDocuments.stream()
                .mapToLong(Document::getFileSize)
                .sum();
        stats.put("totalStorageBytes", totalStorage);
        stats.put("totalStorageMB", totalStorage / (1024.0 * 1024.0));

        // Recent uploads (last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        long recentUploads = projectDocuments.stream()
                .filter(doc -> doc.getUploadedAt() != null && doc.getUploadedAt().isAfter(sevenDaysAgo))
                .count();
        stats.put("recentUploads", recentUploads);

        return stats;
    }

    public Map<String, Object> calculateProjectProgress(String projectId) {
        log.info("Calculating project progress based on documents: {}", projectId);

        List<Document> projectDocuments = documentRepository.findByEntityIdAndEntityType(
                projectId,
                EntityType.PROJECT
        );

        Map<String, Object> progress = new HashMap<>();

        // Progress by phase/category
        Map<DocumentCategory, Double> progressByCategory = new HashMap<>();

        for (DocumentCategory category : DocumentCategory.values()) {
            long categoryDocs = projectDocuments.stream()
                    .filter(doc -> doc.getCategory() == category)
                    .filter(doc -> doc.getDocumentType() == DocumentType.COMPLETION_EVIDENCE
                            || doc.getDocumentType() == DocumentType.PROGRESS_PHOTO)
                    .count();

            // Simple calculation: assume each category needs minimum 10 documents
            double categoryProgress = Math.min(100.0, (categoryDocs / 10.0) * 100);
            progressByCategory.put(category, categoryProgress);
        }

        progress.put("progressByCategory", progressByCategory);

        // Overall progress (average of all categories)
        double overallProgress = progressByCategory.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        progress.put("overallProgress", Math.round(overallProgress * 100.0) / 100.0);

        // Completed tasks (based on completion evidence)
        long completedTasks = projectDocuments.stream()
                .filter(doc -> doc.getDocumentType() == DocumentType.COMPLETION_EVIDENCE)
                .filter(doc -> doc.getApprovalStatus() == DocumentApprovalStatus.APPROVED)
                .map(Document::getTaskId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        progress.put("completedTasks", completedTasks);

        // Last update
        Optional<LocalDateTime> lastUpdate = projectDocuments.stream()
                .map(Document::getUploadedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo);

        lastUpdate.ifPresent(dateTime -> progress.put("lastUpdate", dateTime));

        return progress;
    }

    // ==================== HELPER METHODS ====================

    private String generateEnhancedFolderPath(EnhancedDocumentUploadRequest request) {
        StringBuilder path = new StringBuilder();

        if (request.getCompanyId() != null) {
            path.append(request.getCompanyId()).append("/");
        }

        if (request.getEntityType() != null) {
            path.append(request.getEntityType().name().toLowerCase()).append("/");
        }

        if (request.getEntityId() != null) {
            path.append(request.getEntityId()).append("/");
        }

        if (request.getTaskId() != null) {
            path.append("task-").append(request.getTaskId()).append("/");
        }

        if (request.getCategory() != null) {
            path.append(request.getCategory().name().toLowerCase());
        } else {
            path.append("general");
        }

        return path.toString();
    }

    private String determineMilestone(List<Document> documents) {
        // Logic to determine if these documents represent a milestone
        long completionDocs = documents.stream()
                .filter(doc -> doc.getDocumentType() == DocumentType.COMPLETION_EVIDENCE)
                .count();

        if (completionDocs > 0) {
            return "Phase Completed";
        }

        long inspectionDocs = documents.stream()
                .filter(doc -> doc.getDocumentType() == DocumentType.INSPECTION_REPORT)
                .count();

        if (inspectionDocs > 0) {
            return "Inspection Completed";
        }

        return null;
    }

    private Integer calculatePhaseProgress(List<Document> documents) {
        // Simple calculation based on document types
        long totalDocs = documents.size();
        long approvedDocs = documents.stream()
                .filter(doc -> doc.getApprovalStatus() == DocumentApprovalStatus.APPROVED)
                .count();

        if (totalDocs == 0) return 0;

        return (int) ((approvedDocs * 100.0) / totalDocs);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Max file size: 100MB
        long maxSize = 100 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("File size exceeds maximum limit (100MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new RuntimeException("Invalid file type");
        }
    }

    private String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        int lastSlash = filePath.lastIndexOf("/");
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDot = fileName.lastIndexOf(".");
        return lastDot > 0 && lastDot < fileName.length() - 1
                ? fileName.substring(lastDot + 1).toLowerCase()
                : "";
    }

    private DocumentDto toDto(Document document) {
        String downloadUrl = null;
        try {
            downloadUrl = storageService.getPresignedUrl(document.getFilePath(), 60);
        } catch (Exception e) {
            log.warn("Failed to generate download URL for document {}", document.getId());
        }

        return DocumentDto.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .originalFileName(document.getOriginalFileName())
                .fileSize(document.getFileSize())
                .mimeType(document.getMimeType())
                .fileExtension(document.getFileExtension())
                .documentType(document.getDocumentType())
                .category(document.getCategory())
                .description(document.getDescription())
                .tags(document.getTags())
                .entityType(document.getEntityType())
                .entityId(document.getEntityId())
                .uploadedBy(document.getUploadedBy())
                .companyId(document.getCompanyId())
                .uploadedAt(document.getUploadedAt())
                .isPublic(document.getIsPublic())
                .version(document.getVersion())
                .status(document.getStatus())
                .downloadUrl(downloadUrl)
                .taskId(document.getTaskId())
                .phaseId(document.getPhaseId())
                .latitude(document.getLatitude())
                .longitude(document.getLongitude())
                .locationDescription(document.getLocationDescription())
                .workerId(document.getWorkerId())
                .comments(document.getComments())
                .approvalStatus(document.getApprovalStatus())
                .approvedBy(document.getApprovedBy())
                .approverComments(document.getApproverComments())
                .qualityRating(document.getQualityRating())
                .build();
    }

    // ==================== EVENT PUBLISHING ====================

    private void publishProgressDocumentEvent(Document document, EnhancedDocumentUploadRequest request) {
        try {
            ProgressDocumentUploadedEvent event = ProgressDocumentUploadedEvent.builder()
                    .documentId(document.getId())
                    .taskId(document.getTaskId())
                    .unitId(document.getEntityType() == EntityType.UNIT ? document.getEntityId() : null)
                    .projectId(document.getEntityType() == EntityType.PROJECT ? document.getEntityId() : null)
                    .workerId(document.getWorkerId())
                    .fileName(document.getOriginalFileName())
                    .documentType(document.getDocumentType().name())
                    .completionPercentage(request.getCompletionPercentage())
                    .notifyUsers(request.getNotifyUsers())
                    .latitude(document.getLatitude())
                    .longitude(document.getLongitude())
                    .folderId(document.getFolderId())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("progress-document-events", event);
            log.info("Progress document event published: {}", document.getId());
        } catch (Exception e) {
            log.error("Failed to publish progress document event: {}", e.getMessage());
        }
    }

    private void publishDocumentApprovedEvent(Document document, DocumentApprovalRequest request) {
        try {
            DocumentApprovedEvent event = DocumentApprovedEvent.builder()
                    .documentId(document.getId())
                    .taskId(document.getTaskId())
                    .approvedBy(document.getApprovedBy())
                    .approvalStatus(document.getApprovalStatus().name())
                    .comments(document.getApproverComments())
                    .updateTaskProgress(request.getUpdateTaskProgress())
                    .folderId(document.getFolderId())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("document-approval-events", event);
            log.info("Document approval event published: {}", document.getId());
        } catch (Exception e) {
            log.error("Failed to publish document approval event: {}", e.getMessage());
        }
    }
}