package com.strux.project_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhaseProgressUpdatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String projectId;
    private String companyId;
    private String phaseId;
    private String phaseName;
    private BigDecimal previousProgress;
    private BigDecimal currentProgress;
    private BigDecimal progressChange;
    private LocalDateTime timestamp;
}
