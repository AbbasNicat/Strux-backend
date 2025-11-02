package com.strux.project_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDeletedEvent {
    private String projectId;
    private String companyId;
    private String projectName;
    private String reason;
    private String deletedBy;
    private LocalDateTime timestamp;
}
