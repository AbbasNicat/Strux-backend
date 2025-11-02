package com.strux.document_service.service;

import com.strux.document_service.dto.*;
import com.strux.document_service.enums.*;
import com.strux.document_service.model.Document;
import com.strux.document_service.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final MinioStorageService storageService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public DocumentDto uploadDocument(MultipartFile file, DocumentUploadRequest request, String uploadedBy) {
        try {
            // Validate file
            validateFile(file);

            // Generate folder path based on entity
            String folder = generateFolderPath(request);

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
                    .build();

            document = documentRepository.save(document);

            // Publish event
            publishDocumentUploadedEvent(document);

            return toDto(document);

        } catch (Exception e) {
            log.error("Error uploading document: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload document: " + e.getMessage());
        }
    }

    @Transactional
    public BulkUploadResponse bulkUpload(List<MultipartFile> files, DocumentUploadRequest request, String uploadedBy) {
        List<DocumentDto> uploadedDocuments = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                DocumentDto doc = uploadDocument(file, request, uploadedBy);
                uploadedDocuments.add(doc);
            } catch (Exception e) {
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        return BulkUploadResponse.builder()
                .totalFiles(files.size())
                .successCount(uploadedDocuments.size())
                .failedCount(errors.size())
                .uploadedDocuments(uploadedDocuments)
                .errors(errors)
                .build();
    }

    public InputStream downloadDocument(String documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        return storageService.downloadFile(document.getFilePath());
    }

    public String getDownloadUrl(String documentId, int expiryMinutes) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        return storageService.getPresignedUrl(document.getFilePath(), expiryMinutes);
    }

    public DocumentDto getDocument(String documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        return toDto(document);
    }

    public List<DocumentDto> getDocumentsByEntity(EntityType entityType, String entityId) {
        return documentRepository.findByEntityTypeAndEntityIdAndStatus(entityType, entityId, DocumentStatus.ACTIVE)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<DocumentDto> getDocumentsByCompany(String companyId) {
        return documentRepository.findByCompanyIdAndStatus(companyId, DocumentStatus.ACTIVE)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<DocumentDto> searchDocuments(DocumentSearchRequest request) {
        // Simple search - can be enhanced with Specifications
        List<Document> documents = documentRepository.findAll();

        return documents.stream()
                .filter(doc -> matchesSearchCriteria(doc, request))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DocumentDto updateDocument(String documentId, DocumentUpdateRequest request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (request.getDescription() != null) {
            document.setDescription(request.getDescription());
        }
        if (request.getDocumentType() != null) {
            document.setDocumentType(request.getDocumentType());
        }
        if (request.getCategory() != null) {
            document.setCategory(request.getCategory());
        }
        if (request.getTags() != null) {
            document.setTags(request.getTags());
        }
        if (request.getIsPublic() != null) {
            document.setIsPublic(request.getIsPublic());
        }

        document = documentRepository.save(document);

        return toDto(document);
    }

    @Transactional
    public void deleteDocument(String documentId, boolean hardDelete) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (hardDelete) {
            // Delete from storage
            storageService.deleteFile(document.getFilePath());
            // Delete from database
            documentRepository.delete(document);
        } else {
            // Soft delete
            document.setStatus(DocumentStatus.DELETED);
            document.setDeletedAt(LocalDateTime.now());
            documentRepository.save(document);
        }

        publishDocumentDeletedEvent(document, hardDelete);
    }

    @Transactional
    public void archiveDocument(String documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.setStatus(DocumentStatus.ARCHIVED);
        document.setArchivedAt(LocalDateTime.now());
        documentRepository.save(document);
    }

    public Map<String, Object> getStorageStats(String companyId) {
        Long totalStorage = documentRepository.getTotalStorageByCompany(companyId);
        Long documentCount = documentRepository.countByCompanyIdAndStatus(companyId, DocumentStatus.ACTIVE);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStorageBytes", totalStorage != null ? totalStorage : 0);
        stats.put("totalStorageMB", totalStorage != null ? totalStorage / (1024 * 1024) : 0);
        stats.put("documentCount", documentCount);

        return stats;
    }

    // Helper methods

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Max file size: 100MB
        long maxSize = 100 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("File size exceeds maximum limit (100MB)");
        }

        // Validate file type (example)
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new RuntimeException("Invalid file type");
        }
    }

    private String generateFolderPath(DocumentUploadRequest request) {
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

        if (request.getCategory() != null) {
            path.append(request.getCategory().name().toLowerCase());
        } else {
            path.append("general");
        }

        return path.toString();
    }

    private String extractFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "";
    }

    private boolean matchesSearchCriteria(Document doc, DocumentSearchRequest request) {
        if (request.getFileName() != null && !doc.getFileName().contains(request.getFileName())) {
            return false;
        }
        if (request.getDocumentType() != null && doc.getDocumentType() != request.getDocumentType()) {
            return false;
        }
        if (request.getCategory() != null && doc.getCategory() != request.getCategory()) {
            return false;
        }
        if (request.getEntityType() != null && doc.getEntityType() != request.getEntityType()) {
            return false;
        }
        if (request.getEntityId() != null && !request.getEntityId().equals(doc.getEntityId())) {
            return false;
        }
        if (request.getCompanyId() != null && !request.getCompanyId().equals(doc.getCompanyId())) {
            return false;
        }
        return true;
    }

    private DocumentDto toDto(Document document) {
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
                .downloadUrl(storageService.getPresignedUrl(document.getFilePath(), 60)) // 1 hour
                .build();
    }

    private void publishDocumentUploadedEvent(Document document) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "document.uploaded");
        event.put("documentId", document.getId());
        event.put("fileName", document.getOriginalFileName());
        event.put("entityType", document.getEntityType());
        event.put("entityId", document.getEntityId());
        event.put("uploadedBy", document.getUploadedBy());
        event.put("companyId", document.getCompanyId());
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send("document.uploaded", event);
        log.info("Document uploaded event published: {}", document.getId());
    }

    private void publishDocumentDeletedEvent(Document document, boolean hardDelete) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "document.deleted");
        event.put("documentId", document.getId());
        event.put("fileName", document.getOriginalFileName());
        event.put("hardDelete", hardDelete);
        event.put("entityType", document.getEntityType());
        event.put("entityId", document.getEntityId());
        event.put("companyId", document.getCompanyId());
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send("document.deleted", event);
        log.info("Document deleted event published: {}", document.getId());
    }
}
