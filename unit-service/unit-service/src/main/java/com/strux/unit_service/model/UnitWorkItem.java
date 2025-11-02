package com.strux.unit_service.model;


import com.strux.unit_service.enums.WorkItemStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "unit_work_items")
public class UnitWorkItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String unitId;

    private String workName;  // "Beton dökme", "Sıva işi", "Elektrik çekimi"
    private String description;

    @Enumerated(EnumType.STRING)
    private WorkItemStatus status;

    private Integer completionPercentage;  // 0-100
    private Integer weightPercentage;  // Bu işin ümumi tamamlanmadakı çəkisi (20%)

    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;

    private String assignedContractorId;
    private String assignedWorkerId;

    private String taskId;  // Task service-dən task ID

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
