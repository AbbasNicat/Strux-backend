package com.strux.project_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUpdatedEvent {
    private String projectId;
    private String companyId;
    private List<String> changedFields;
    private String updatedBy;
    private LocalDateTime timestamp;
}
