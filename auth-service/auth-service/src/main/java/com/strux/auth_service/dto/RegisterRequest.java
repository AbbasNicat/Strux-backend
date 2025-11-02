package com.strux.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email required")
    @Email(message = "Email")
    private String email;

    @NotBlank(message = "Password required")
    @Size(min = 8, message = "Password must be at least 8")
    private String password;

    @NotBlank(message = "Firstname required")
    private String firstName;

    @NotBlank(message = "Lastname required")
    private String lastName;

    private String phone;

    @NotNull
    private UserRole role;  // WORKER, MANAGER, OWNER, ADMIN

    private String companyId;

    private String position;

}
