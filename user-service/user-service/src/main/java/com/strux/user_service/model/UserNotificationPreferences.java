package com.strux.user_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_notification_preferences")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserNotificationPreferences {

    @Id
    private String userId;

    // Genel bildirimler
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean pushNotifications;

    // ❌ EKSİK: Strux'a özel bildirimler

    // İşçi için
    private Boolean notifyOnTaskAssignment;
    private Boolean notifyOnTaskDeadline;
    private Boolean notifyOnPaymentReceived;

    // Şirket admini için
    private Boolean notifyOnWorkerIssueReport;
    private Boolean notifyOnProjectMilestone;
    private Boolean notifyOnDelayedTask;

    // Konut sahibi için
    private Boolean notifyOnProgressUpdate;
    private Boolean notifyOnProjectCompletion;
    private Boolean notifyOnIssueReported;
    private Boolean notifyOnScheduleChange;

    // Haftalık özet
    private Boolean weeklyDigest;
    private Integer weeklyDigestDay; // 1=Pazartesi, 7=Pazar
}
