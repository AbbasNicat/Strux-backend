package com.strux.notification_service.kafka;

import com.strux.notification_service.client.UserServiceClient;
import com.strux.notification_service.enums.NotificationType;
import com.strux.notification_service.model.Notification;
import com.strux.notification_service.repository.NotificationChannelHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailChannelHandler implements NotificationChannelHandler {

    private final JavaMailSender mailSender;
    private final UserServiceClient userServiceClient;

    @Value("${spring.mail.from:noreply@strux.com}")
    private String fromEmail;

    @Override
    public boolean supports(NotificationType type) {
        return type == NotificationType.EMAIL;
    }

    @Override
    public boolean send(Notification notification) {
        try {
            String email = getUserEmail(notification.getUserId());

            if (email == null || email.isEmpty()) {
                log.warn("❌ No email found for user: {}", notification.getUserId());
                return false;
            }

            sendHtmlEmail(email, notification.getTitle(), notification.getMessage());

            log.info("✅ Email sent to: {} (userId: {})", email, notification.getUserId());
            return true;

        } catch (Exception e) {
            log.error("❌ Email send failed for user {}: {}",
                    notification.getUserId(), e.getMessage(), e);
            return false;
        }
    }

    private String getUserEmail(String userId) {
        try {
            return userServiceClient.getUserEmail(userId);
        } catch (Exception e) {
            log.error("❌ Failed to get email from user-service for user {}: {}",
                    userId, e.getMessage());
            return null;
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom(fromEmail);

        String htmlBody = buildHtmlTemplate(subject, htmlContent);
        helper.setText(htmlBody, true);

        mailSender.send(mimeMessage);
    }

    private String buildHtmlTemplate(String title, String content) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        line-height: 1.6; 
                        color: #333; 
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 20px auto; 
                        background: white;
                        border-radius: 8px;
                        overflow: hidden;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    .header { 
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white; 
                        padding: 30px 20px; 
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                    }
                    .content { 
                        padding: 30px 20px;
                        background: #ffffff;
                    }
                    .footer { 
                        text-align: center; 
                        padding: 20px;
                        background: #f9fafb;
                        color: #6b7280; 
                        font-size: 12px;
                        border-top: 1px solid #e5e7eb;
                    }
                    .footer p {
                        margin: 5px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        %s
                    </div>
                    <div class="footer">
                        <p>Bu otomatik bir bildirim mesajıdır. Lütfen yanıt vermeyin.</p>
                        <p>&copy; 2025 Strux. Tüm hakları saklıdır.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(title, content);
    }
}