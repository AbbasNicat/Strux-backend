package com.strux.user_service.event;

import com.strux.user_service.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {
    private String keycloakId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDateTime registeredAt;
    private UserRole role;
    private String companyId;
    private String position;
}
