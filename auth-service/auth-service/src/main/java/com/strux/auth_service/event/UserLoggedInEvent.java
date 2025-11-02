package com.strux.auth_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoggedInEvent {

    private String userId;
    private String email;
    private String ipAddress;
    private LocalDateTime timestamp;
}

