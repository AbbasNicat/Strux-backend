package com.strux.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerInfoDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profession;
    private String avatarUrl;
    private Integer assignedTasksCount;
    private Integer completedTasksCount;
    private LocalDateTime lastActivityDate;
}
