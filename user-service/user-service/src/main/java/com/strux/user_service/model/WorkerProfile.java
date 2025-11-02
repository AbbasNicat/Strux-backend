package com.strux.user_service.model;

import com.strux.user_service.enums.WorkerSpecialty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerProfile {

    @Enumerated(EnumType.STRING)
    private WorkerSpecialty specialty;

    private Integer experienceYears;
    private BigDecimal hourlyRate;
    private BigDecimal rating;
    private Integer completedTasks;
    private Integer totalWorkDays;
    private Integer onTimeCompletionCount;
    private Integer lateCompletionCount;
    private BigDecimal reliabilityScore;
    @ElementCollection
    private List<String> activeProjectIds;
    @Column(nullable = false, name = "worker_is_available")
    @Builder.Default
    private Boolean isAvailable = false;
    private LocalDate availableFrom;
}
