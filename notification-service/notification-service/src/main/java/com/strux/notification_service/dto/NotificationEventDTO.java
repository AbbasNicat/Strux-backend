package com.strux.notification_service.dto;

import com.strux.notification_service.enums.NotificationCategory;
import com.strux.notification_service.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor  // ← BU ÇOK ÖNEMLİ
@AllArgsConstructor
public class NotificationEventDTO {
    private String eventType;
    private String userId;
    private List<String> userIds;
    private String companyId;

    private Map<String, Object> data;

    private NotificationCategory category;
    private List<NotificationType> channels;
}

