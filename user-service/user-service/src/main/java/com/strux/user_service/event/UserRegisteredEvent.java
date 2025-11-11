package com.strux.user_service.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.strux.user_service.enums.UserRole;
import com.strux.user_service.enums.WorkerSpecialty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {
    // TEMEL
    @JsonAlias({"userId", "keycloakId"})
    private String keycloakId; // producer bazen userId diye gönderebilir

    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDateTime registeredAt;
    private UserRole role;
    private String companyId;
    private String position;

    // WORKER
    private WorkerProfileData workerProfile;
    private String city;

    // HOMEOWNER
    private String bio;

    // getUserId alias'ı (mevcut çağrıları kırmamak için)
    public String getUserId() {
        return keycloakId;
    }
    public void setUserId(String userId) {
        this.keycloakId = userId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkerProfileData {
        private WorkerSpecialty specialty;
        private Integer experienceYears;
        private BigDecimal hourlyRate;
    }
}
