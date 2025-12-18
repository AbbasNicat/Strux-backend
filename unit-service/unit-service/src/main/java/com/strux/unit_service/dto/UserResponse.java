package com.strux.unit_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String keycloakId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String city;
    private String avatarUrl;
    private String role;
    private String status;
    private String companyId;
    private Boolean isAvailable;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Worker Profile fields
    private String specialty;
    private Integer experienceYears;
    private BigDecimal hourlyRate;
    private BigDecimal rating;
    private Integer completedTasks;
    private Integer totalWorkDays;
    private BigDecimal reliabilityScore;

    private LocalDateTime saleDate;


    // Unit assignments
    private Set<String> assignedUnitIds;
}