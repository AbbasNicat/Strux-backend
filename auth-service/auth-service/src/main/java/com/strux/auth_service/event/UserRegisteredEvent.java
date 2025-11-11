package com.strux.auth_service.event;

import com.strux.auth_service.dto.UserRole;
import com.strux.auth_service.dto.WorkerSpecialty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisteredEvent {
    private String keycloakId;   // <<< önce userId idi, keycloakId yap
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private LocalDateTime registeredAt;
    private UserRole role;
    private String companyId;
    private String position;

    // ✅ YENİ: Worker-specific fields (nullable)
    private WorkerProfileData workerProfile;
    private String city; // Worker'lar için önemli

    // ✅ YENİ: Homeowner-specific fields (nullable)
    private String bio;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkerProfileData {
        private WorkerSpecialty specialty;
        private Integer experienceYears;
        private BigDecimal hourlyRate;
    }

    // ✅ Builder pattern için constructor
    public UserRegisteredEvent(String keycloakId, String firstName, String lastName,
                               String phone, String email, LocalDateTime registeredAt,
                               UserRole role, String companyId, String position) {
        this.keycloakId = keycloakId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.registeredAt = registeredAt;
        this.role = role;
        this.companyId = companyId;
        this.position = position;
    }
}