package com.strux.user_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoggedInEvent {

    private String userId;
    private String email;
    private String ipAddress;
    private LocalDateTime loginTime;
}

