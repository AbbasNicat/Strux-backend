package com.strux.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignWorkerRequest {

    @NotNull(message = "Worker ID is required")
    private String workerId;

    @NotBlank(message = "Project ID is required")
    private String projectId;

    private String assignedBy;
    private String taskDescription;
}
