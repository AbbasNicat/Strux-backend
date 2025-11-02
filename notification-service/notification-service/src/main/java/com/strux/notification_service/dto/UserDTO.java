package com.strux.notification_service.dto;

import com.strux.notification_service.enums.NotificationCategory;
import com.strux.notification_service.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;
    private String email;
    private String phoneNumber;
    private String fcmToken;
    private String firstName;
    private String lastName;
}
