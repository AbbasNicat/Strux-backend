package com.strux.user_service.dto;

import com.strux.user_service.enums.UserRole;
import com.strux.user_service.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.strux.user_service.enums.UserStatus;
import com.strux.user_service.model.WorkerProfile;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String keycloakId;
    private String phone;
    private UserRole role;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime verifiedAt;
    private String city;
    private String message;

    private WorkerProfileResponse  workerProfile;

    private String provider; // "LOCAL", "GOOGLE", "GITHUB"
    private String providerId;
    private Boolean twoFaEnabled;
    private UserStatus status;
    private String companyId;
    private String position;
    private String bio;
    private String profileImageUrl;
    private Boolean emailVerified;
    private Boolean phoneVerified;

    private Set<String> assignedUnitIds;
}