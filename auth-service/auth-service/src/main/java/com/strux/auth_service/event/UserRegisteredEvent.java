package com.strux.auth_service.event;

import com.strux.auth_service.dto.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisteredEvent {

    private String keycloakId;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private LocalDateTime timestamp;

    private UserRole role;
    private String companyId;
    private String position;
}
