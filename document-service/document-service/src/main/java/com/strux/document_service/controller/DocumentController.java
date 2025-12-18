package com.strux.document_service.controller;

import com.strux.document_service.dto.*;
import com.strux.document_service.enums.DocumentCategory;
import com.strux.document_service.enums.DocumentType;
import com.strux.document_service.enums.EntityType;
import com.strux.document_service.service.DocumentService;
import com.strux.document_service.service.FolderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;
    private final FolderService folderService;

    // ==================== FOLDER ENDPOINTS ====================

    @PostMapping("/folders")
    public ResponseEntity<FolderDto> createFolder(
            @RequestBody @Valid FolderCreateRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        log.info("Creating folder: {} by user: {}", request.getName(), userId);
        FolderDto folder = folderService.createFolder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(folder);
    }

    @GetMapping("/folders/{folderId}")
    public ResponseEntity<FolderDto> getFolder(@PathVariable String folderId) {
        FolderDto folder = folderService.getFolder(folderId);
        return ResponseEntity.ok(folder);
    }

    @GetMapping("/folders/company/{companyId}/root")
    public ResponseEntity<List<FolderDto>> getRootFolders(@PathVariable String companyId) {
        List<FolderDto> folders = folderService.getRootFolders(companyId);
        return ResponseEntity.ok(folders);
    }

    @GetMapping("/folders/{folderId}/subfolders")
    public ResponseEntity<List<FolderDto>> getSubFolders(@PathVariable String folderId) {
        List<FolderDto> folders = folderService.getSubFolders(folderId);
        return ResponseEntity.ok(folders);
    }

    @GetMapping("/folders/{folderId}/tree")
    public ResponseEntity<FolderDto> getFolderTree(@PathVariable String folderId) {
        FolderDto tree = folderService.getFolderTree(folderId);
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/folders/company/{companyId}/tree")
    public ResponseEntity<List<FolderDto>> getCompanyFolderTree(@PathVariable String companyId) {
        log.info("Getting folder tree for company: {}", companyId);
        List<FolderDto> tree = folderService.getCompanyFolderTree(companyId);
        return ResponseEntity.ok(tree);
    }

    @PutMapping("/folders/{folderId}")
    public ResponseEntity<FolderDto> updateFolder(
            @PathVariable String folderId,
            @RequestBody @Valid FolderCreateRequest request
    ) {
        FolderDto folder = folderService.updateFolder(folderId, request);
        return ResponseEntity.ok(folder);
    }

    @DeleteMapping("/folders/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @PathVariable String folderId,
            @RequestParam(defaultValue = "false") boolean deleteDocuments
    ) {
        folderService.deleteFolder(folderId, deleteDocuments);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/folders/{folderId}/move")
    public ResponseEntity<Void> moveFolder(
            @PathVariable String folderId,
            @RequestParam(required = false) String newParentFolderId
    ) {
        folderService.moveFolder(folderId, newParentFolderId);
        return ResponseEntity.ok().build();
    }

    // ==================== DOCUMENT ENDPOINTS ====================

    @PutMapping("/{documentId}/move")
    public ResponseEntity<DocumentDto> moveDocument(
            @PathVariable String documentId,
            @RequestParam(required = false) String folderId
    ) {
        log.info("Moving document {} to folder: {}", documentId, folderId);
        DocumentDto document = documentService.moveDocument(documentId, folderId);
        return ResponseEntity.ok(document);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDto> uploadDocument(
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") @Valid DocumentUploadRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            HttpServletRequest httpRequest
    ) {
        log.info("========== UPLOAD REQUEST START ==========");
        log.info("📥 Content-Type: {}", httpRequest.getContentType());
        log.info("📥 Method: {}", httpRequest.getMethod());
        log.info("📥 Request URI: {}", httpRequest.getRequestURI());

        log.info("📥 File: {}", file != null ? "Present" : "NULL");
        if (file != null) {
            log.info("   - Name: {}", file.getOriginalFilename());
            log.info("   - Size: {} bytes", file.getSize());
            log.info("   - Type: {}", file.getContentType());
        }

        log.info("📥 Request: {}", request != null ? "Present" : "NULL");
        if (request != null) {
            log.info("   - Company ID: {}", request.getCompanyId());
            log.info("   - Entity Type: {}", request.getEntityType());
        }

        log.info("📥 User ID: {}", userId);
        log.info("==========================================");

        DocumentDto document = documentService.uploadDocument(file, request, userId);
        log.info("✅ Document uploaded successfully: {}", document.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }

    @PostMapping(value = "/avatar/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestPart("file") MultipartFile file,
            @RequestParam("companyId") String companyId,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") String entityId,
            @RequestParam("documentType") String documentType,
            @RequestParam("category") String category,
            @RequestParam(value = "description", required = false) String description,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        try {
            log.info("========== AVATAR UPLOAD START ==========");
            log.info("📥 File: {}, Size: {}", file.getOriginalFilename(), file.getSize());
            log.info("📥 companyId: {}", companyId);
            log.info("📥 entityType: {}", entityType);
            log.info("📥 entityId: {}", entityId);
            log.info("📥 documentType: {}", documentType);
            log.info("📥 category: {}", category);
            log.info("📥 userId: {}", userId);

            DocumentUploadRequest request = new DocumentUploadRequest();
            request.setCompanyId(companyId);

            log.info("🔄 Parsing EntityType: {}", entityType);
            request.setEntityType(EntityType.valueOf(entityType));

            request.setEntityId(entityId);

            log.info("🔄 Parsing DocumentType: {}", documentType);
            request.setDocumentType(DocumentType.valueOf(documentType));

            log.info("🔄 Parsing DocumentCategory: {}", category);
            request.setCategory(DocumentCategory.valueOf(category));

            request.setDescription(description);

            log.info("🔄 Calling documentService.uploadDocument()");
            DocumentDto document = documentService.uploadDocument(file, request, userId);

            log.info("✅ Upload successful, documentId: {}", document.getId());

            return ResponseEntity.ok(Map.of(
                    "fileUrl", document.getDownloadUrl(),
                    "downloadUrl", document.getDownloadUrl(),
                    "documentId", document.getId()
            ));

        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid enum value: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid enum: " + e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Upload failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/upload-form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDto> uploadDocumentForm(
            @RequestPart("file") MultipartFile file,
            @Valid @ModelAttribute DocumentUploadRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        log.info("Uploading document (form): {} by user: {}", file.getOriginalFilename(), userId);
        DocumentDto document = documentService.uploadDocument(file, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }

    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkUploadResponse> bulkUpload(
            @RequestPart("files") List<MultipartFile> files,
            @RequestPart("request") @Valid DocumentUploadRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        log.info("Bulk uploading {} documents by user: {}", files.size(), userId);
        BulkUploadResponse response = documentService.bulkUpload(files, request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentDto> getDocument(@PathVariable String documentId) {
        DocumentDto document = documentService.getDocument(documentId);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<InputStreamResource> downloadDocument(@PathVariable String documentId) {
        DocumentDto document = documentService.getDocument(documentId);
        InputStream inputStream = documentService.downloadDocument(documentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(document.getMimeType()))
                .body(new InputStreamResource(inputStream));
    }

    @GetMapping("/{documentId}/download-url")
    public ResponseEntity<Map<String, String>> getDownloadUrl(
            @PathVariable String documentId,
            @RequestParam(defaultValue = "60") int expiryMinutes
    ) {
        String url = documentService.getDownloadUrl(documentId, expiryMinutes);
        return ResponseEntity.ok(Map.of("downloadUrl", url));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<DocumentDto>> getDocumentsByEntity(
            @PathVariable EntityType entityType,
            @PathVariable String entityId
    ) {
        List<DocumentDto> documents = documentService.getDocumentsByEntity(entityType, entityId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<DocumentDto>> getDocumentsByCompany(@PathVariable String companyId) {
        List<DocumentDto> documents = documentService.getDocumentsByCompany(companyId);
        return ResponseEntity.ok(documents);
    }

    @PostMapping("/search")
    public ResponseEntity<List<DocumentDto>> searchDocuments(
            @RequestBody DocumentSearchRequest request
    ) {
        List<DocumentDto> documents = documentService.searchDocuments(request);
        return ResponseEntity.ok(documents);
    }

    @PutMapping("/{documentId}")
    public ResponseEntity<DocumentDto> updateDocument(
            @PathVariable String documentId,
            @RequestBody @Valid DocumentUpdateRequest request
    ) {
        DocumentDto document = documentService.updateDocument(documentId, request);
        return ResponseEntity.ok(document);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable String documentId,
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        documentService.deleteDocument(documentId, hardDelete);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{documentId}/archive")
    public ResponseEntity<Void> archiveDocument(@PathVariable String documentId) {
        documentService.archiveDocument(documentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/company/{companyId}/stats")
    public ResponseEntity<Map<String, Object>> getStorageStats(@PathVariable String companyId) {
        Map<String, Object> stats = documentService.getStorageStats(companyId);
        return ResponseEntity.ok(stats);
    }
}