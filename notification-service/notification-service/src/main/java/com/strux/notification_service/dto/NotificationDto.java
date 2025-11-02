package com.strux.notification_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class NotificationDto {
    private String id;
    private String title;
    private String message;
    private String category;
    private String type;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private Map<String, Object> data;
}
