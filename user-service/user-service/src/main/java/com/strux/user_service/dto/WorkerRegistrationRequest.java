package com.strux.user_service.dto;

import com.strux.user_service.enums.WorkerSpecialty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerRegistrationRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String phone;
    private String city;
    private String companyId;

    @NotNull
    private WorkerSpecialty specialty;

    private Integer experienceYears;
    private BigDecimal hourlyRate;
}
