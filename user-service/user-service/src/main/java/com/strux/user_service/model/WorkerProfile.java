package com.strux.user_service.model;

import com.strux.user_service.enums.WorkerSpecialty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "worker_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerProfile {

    @Id
    @Column(name = "user_id")
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialty")
    private WorkerSpecialty specialty;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    @Column(name = "rating")
    private BigDecimal rating;

    @Column(name = "completed_tasks")
    private Integer completedTasks;

    @Column(name = "total_work_days")
    private Integer totalWorkDays;

    @Column(name = "on_time_completion_count")
    private Integer onTimeCompletionCount;

    @Column(name = "late_completion_count")
    private Integer lateCompletionCount;

    @Column(name = "reliability_score")
    private BigDecimal reliabilityScore;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "available_from")
    private LocalDate availableFrom;

    @ElementCollection
    @CollectionTable(name = "user_active_project_ids",
            joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "active_project_ids")
    @Builder.Default
    private List<String> activeProjectIds = new ArrayList<>();

    // ✅ YENİ: Unit atamaları için
    @ElementCollection
    @CollectionTable(name = "worker_assigned_units",
            joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "unit_id")
    @Builder.Default
    private Set<String> assignedUnitIds = new HashSet<>();
}