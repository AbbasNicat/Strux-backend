package com.strux.unit_service.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDateTime;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionRecord {

    private String inspectionId;
    private String inspectorId;
    private String inspectorName;
    private LocalDateTime inspectionDate;
    private Integer score;  // 1-100
    private String notes;
    private Boolean passed;
}
