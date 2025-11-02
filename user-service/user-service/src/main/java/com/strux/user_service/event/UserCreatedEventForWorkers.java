package com.strux.user_service.event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEventForWorkers {
    private String keycloakId;
    private String userId;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String role;
}
