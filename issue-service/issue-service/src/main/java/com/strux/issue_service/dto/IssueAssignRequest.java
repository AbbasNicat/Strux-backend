package com.strux.issue_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueAssignRequest {

    @NotBlank(message = "Assigned user ID is required")
    private String assignedTo;
}
