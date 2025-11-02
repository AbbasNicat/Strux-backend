package com.strux.notification_service.kafka;

import com.google.firebase.messaging.*;
import com.strux.notification_service.client.UserServiceClient;
import com.strux.notification_service.enums.NotificationType;
import com.strux.notification_service.model.Notification;
import com.strux.notification_service.repository.NotificationChannelHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PushChannelHandler implements NotificationChannelHandler {

    private final FirebaseMessaging firebaseMessaging;
    private final UserServiceClient userServiceClient;

    @Override
    public boolean supports(NotificationType type) {
        return type == NotificationType.PUSH;
    }

    @Override
    public boolean send(Notification notification) {
        try {
            // User Service'den device token'ları al
            List<String> deviceTokens = getDeviceTokens(notification.getUserId());

            if (deviceTokens == null || deviceTokens.isEmpty()) {
                log.warn("❌ No device tokens found for user: {}", notification.getUserId());
                return false;
            }

            // Her device'a gönder
            int successCount = 0;
            for (String token : deviceTokens) {
                if (sendToDevice(token, notification)) {
                    successCount++;
                }
            }

            boolean allSent = successCount == deviceTokens.size();
            log.info("📱 Push notifications sent: {}/{} successful for user: {}",
                    successCount, deviceTokens.size(), notification.getUserId());

            return successCount > 0; // En az 1 device'a gönderildiyse başarılı

        } catch (Exception e) {
            log.error("❌ Push send failed for user {}: {}",
                    notification.getUserId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * User Service'den device token'ları al (Feign ile)
     */
    private List<String> getDeviceTokens(String userId) {
        try {
            List<String> tokens = userServiceClient.getUserDeviceTokens(userId);
            log.debug("📱 Retrieved {} device token(s) for user: {}", tokens.size(), userId);
            return tokens;
        } catch (Exception e) {
            log.error("❌ Failed to get device tokens from user-service for user {}: {}",
                    userId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Tek bir device'a push gönder
     */
    private boolean sendToDevice(String deviceToken, Notification notification) {
        try {
            // FCM Notification
            com.google.firebase.messaging.Notification fcmNotification =
                    com.google.firebase.messaging.Notification.builder()
                            .setTitle(notification.getTitle())
                            .setBody(notification.getMessage())
                            .build();

            // Data payload
            Map<String, String> data = buildDataPayload(notification);

            // Android config
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setTtl(3600 * 1000) // 1 saat
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                            .setSound("default")
                            .setColor("#667eea")
                            .setChannelId("strux_notifications")
                            .setPriority(AndroidNotification.Priority.HIGH)
                            .build())
                    .build();

            // iOS config
            ApnsConfig apnsConfig = ApnsConfig.builder()
                    .setAps(Aps.builder()
                            .setSound("default")
                            .setContentAvailable(true)
                            .build())
                    .build();

            // Message
            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(fcmNotification)
                    .putAllData(data)
                    .setAndroidConfig(androidConfig)
                    .setApnsConfig(apnsConfig)
                    .build();

            // Send
            String response = firebaseMessaging.send(message);
            log.debug("✅ Push sent successfully: {}", response);
            return true;

        } catch (FirebaseMessagingException e) {
            handleFirebaseError(e, deviceToken, notification.getUserId());
            return false;
        } catch (Exception e) {
            log.error("❌ Unexpected error sending push to device: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Data payload oluştur
     */
    private Map<String, String> buildDataPayload(Notification notification) {
        Map<String, String> data = new HashMap<>();
        data.put("notificationId", notification.getId());
        data.put("eventType", notification.getEventType());
        data.put("category", notification.getCategory().name());
        data.put("userId", notification.getUserId());

        // Custom data ekle
        if (notification.getData() != null) {
            notification.getData().forEach((key, value) -> {
                if (value != null) {
                    data.put(key, String.valueOf(value));
                }
            });
        }

        return data;
    }

    /**
     * Firebase error handling
     */
    private void handleFirebaseError(FirebaseMessagingException e, String token, String userId) {
        String errorCode = String.valueOf(e.getErrorCode());

        switch (errorCode) {
            case "NOT_FOUND":
            case "UNREGISTERED":
                log.warn("❌ Invalid/expired device token for user {}: {} (should be deleted)",
                        userId, token.substring(0, Math.min(20, token.length())) + "...");
                // TODO: User Service'e token silme isteği gönder
                break;

            case "INVALID_ARGUMENT":
                log.error("❌ Invalid message format for user {}", userId);
                break;

            case "QUOTA_EXCEEDED":
                log.error("❌ FCM quota exceeded - rate limit hit");
                break;

            case "SENDER_ID_MISMATCH":
                log.error("❌ FCM sender ID mismatch for user {}", userId);
                break;

            case "UNAVAILABLE":
                log.warn("⚠️ FCM temporarily unavailable, retry later");
                break;

            default:
                log.error("❌ Firebase error [{}]: {}", errorCode, e.getMessage());
        }
    }
}