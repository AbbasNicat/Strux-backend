package com.strux.document_service.controller;

import com.strux.document_service.dto.*;
import com.strux.document_service.enums.EntityType;
import com.strux.document_service.service.DocumentService;
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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDto> uploadDocument(
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") @Valid DocumentUploadRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        log.info("Uploading document: {} by user: {}", file.getOriginalFilename(), userId);
        DocumentDto document = documentService.uploadDocument(file, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(document);
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
