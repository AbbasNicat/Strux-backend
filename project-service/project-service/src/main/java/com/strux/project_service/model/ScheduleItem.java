package com.strux.project_service.model;

import com.strux.project_service.enums.ScheduleStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
// schedule servisi ucun
@Entity
@Data
@Table(name = "schedule_items")
public class ScheduleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    private String title;
    private String description;

    private LocalDate plannedDate;
    private LocalDate actualDate;

    @Enumerated(EnumType.STRING)
    private ScheduleStatus status; // PLANNED, COMPLETED, DELAYED

    private Long relatedPhaseId;
}
