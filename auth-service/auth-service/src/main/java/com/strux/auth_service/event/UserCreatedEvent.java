package com.strux.auth_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    
    private String userId;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String role;
}
