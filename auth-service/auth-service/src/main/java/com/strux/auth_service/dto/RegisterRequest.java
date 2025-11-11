package com.strux.auth_service.dto;

import com.strux.auth_service.dto.WorkerSpecialty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    // ============================================
    // TEMEL BİLGİLER (Tüm roller için zorunlu)
    // ============================================
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 12, message = "Password must be at least 12 characters")
    private String password;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;

    @NotNull(message = "Role is required")
    private UserRole role;

    // ============================================
    // ŞİRKET YÖNETİMİ (COMPANY_ADMIN için)
    // ============================================
    @NotBlank(message = "Mode is required (create or join)")
    private String mode; // "create" veya "join"

    private String companyName; // mode = "create" ise zorunlu

    private String inviteCode; // mode = "join" ise zorunlu

    private String position;

    private WorkerSpecialty specialty;

    @Min(value = 0, message = "Experience years must be positive")
    private Integer experienceYears;

    @Min(value = 0, message = "Hourly rate must be positive")
    private BigDecimal hourlyRate;

    private String city;

    private String bio;

    public void validateForRole() {
        if (role == UserRole.COMPANY_ADMIN) {
            if ("create".equals(mode) && (companyName == null || companyName.isBlank())) {
                throw new IllegalArgumentException("Company name is required for creating a new company");
            }
            if ("join".equals(mode) && (inviteCode == null || inviteCode.isBlank())) {
                throw new IllegalArgumentException("Invite code is required for joining a company");
            }
        }

        if (role == UserRole.WORKER) {
            if (specialty == null) {
                throw new IllegalArgumentException("Specialty is required for workers");
            }
            if (experienceYears == null) {
                throw new IllegalArgumentException("Experience years is required for workers");
            }
            if (hourlyRate == null) {
                throw new IllegalArgumentException("Hourly rate is required for workers");
            }
        }
    }
}