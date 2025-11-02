package com.strux.document_service.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TimelineDocumentDto {
    private LocalDateTime date;
    private String phase;
    private List<DocumentDto> documents;
    private String milestone;  // "Foundation completed"
    private Integer progressPercentage;
}
