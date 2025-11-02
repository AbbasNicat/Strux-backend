package com.strux.document_service.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadResponse {

    private Integer totalFiles;
    private Integer successCount;
    private Integer failedCount;
    private List<DocumentDto> uploadedDocuments;
    private List<String> errors;
}
