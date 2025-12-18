package com.strux.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false); // Plain text

            mailSender.send(message);
            log.info("Email sent successfully to: {}", maskEmail(to));
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", maskEmail(to), e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + token;

            String subject = "Verify Your Email - Strux Platform";
            String htmlContent = buildVerificationEmailHtml(verificationUrl);

            sendHtmlEmail(to, subject, htmlContent);

            log.info("Verification email sent successfully to: {}", maskEmail(to));

        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", maskEmail(to), e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            String subject = "Password Reset Request - Strux Platform";
            String htmlContent = buildPasswordResetEmailHtml(resetUrl);

            sendHtmlEmail(to, subject, htmlContent);

            log.info("Password reset email sent successfully to: {}", maskEmail(to));

        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", maskEmail(to), e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Override
    public void sendPasswordChangedEmail(String to) {
        try {
            String subject = "Your Password Has Been Changed - Strux Platform";
            String htmlContent = buildPasswordChangedEmailHtml();

            sendHtmlEmail(to, subject, htmlContent);

            log.info("Password changed email sent successfully to: {}", maskEmail(to));

        } catch (Exception e) {
            log.error("Failed to send password changed email to {}: {}", maskEmail(to), e.getMessage());
            // Don't throw - this is not critical
        }
    }

    @Override
    public void send2FACode(String to, String code) {
        try {
            String subject = "Your Verification Code - Strux Platform";
            String htmlContent = build2FAEmailHtml(code);

            sendHtmlEmail(to, subject, htmlContent);

            log.info("2FA code sent successfully to: {}", maskEmail(to));

        } catch (Exception e) {
            log.error("Failed to send 2FA code to {}: {}", maskEmail(to), e.getMessage());
            throw new RuntimeException("Failed to send 2FA code", e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // HTML

        mailSender.send(message);
    }

    private String buildVerificationEmailHtml(String verificationUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background-color: #2563eb; color: white; padding: 20px; text-align: center; }" +
                "        .content { background-color: #f9fafb; padding: 30px; }" +
                "        .button { display: inline-block; padding: 12px 30px; background-color: #2563eb; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }" +
                "        .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 12px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"container\">" +
                "        <div class=\"header\">" +
                "            <h1>Welcome to Strux!</h1>" +
                "        </div>" +
                "        <div class=\"content\">" +
                "            <h2>Verify Your Email Address</h2>" +
                "            <p>Thank you for signing up with Strux Platform. To complete your registration, please verify your email address by clicking the button below:</p>" +
                "            <div style=\"text-align: center;\">" +
                "                <a href=\"" + verificationUrl + "\" class=\"button\">Verify Email</a>" +
                "            </div>" +
                "            <p>Or copy and paste this link into your browser:</p>" +
                "            <p style=\"word-break: break-all; color: #2563eb;\">" + verificationUrl + "</p>" +
                "            <p style=\"margin-top: 30px; color: #6b7280;\"><strong>Note:</strong> This link will expire in 24 hours.</p>" +
                "            <p style=\"color: #6b7280;\">If you didn't create an account with Strux, please ignore this email.</p>" +
                "        </div>" +
                "        <div class=\"footer\">" +
                "            <p>© 2024 Strux Platform. All rights reserved.</p>" +
                "            <p>This is an automated email, please do not reply.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private String buildPasswordResetEmailHtml(String resetUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background-color: #2563eb; color: white; padding: 20px; text-align: center; }" +
                "        .content { background-color: #f9fafb; padding: 30px; }" +
                "        .button { display: inline-block; padding: 12px 30px; background-color: #2563eb; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }" +
                "        .warning { background-color: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; margin: 20px 0; }" +
                "        .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 12px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"container\">" +
                "        <div class=\"header\">" +
                "            <h1>Password Reset Request</h1>" +
                "        </div>" +
                "        <div class=\"content\">" +
                "            <h2>Reset Your Password</h2>" +
                "            <p>We received a request to reset your password. Click the button below to create a new password:</p>" +
                "            <div style=\"text-align: center;\">" +
                "                <a href=\"" + resetUrl + "\" class=\"button\">Reset Password</a>" +
                "            </div>" +
                "            <p>Or copy and paste this link into your browser:</p>" +
                "            <p style=\"word-break: break-all; color: #2563eb;\">" + resetUrl + "</p>" +
                "            <div class=\"warning\">" +
                "                <p style=\"margin: 0;\"><strong>⚠️ Security Notice:</strong> This link will expire in 1 hour for your security.</p>" +
                "            </div>" +
                "            <p style=\"color: #6b7280;\">If you didn't request a password reset, please ignore this email or contact support if you have concerns.</p>" +
                "        </div>" +
                "        <div class=\"footer\">" +
                "            <p>© 2024 Strux Platform. All rights reserved.</p>" +
                "            <p>This is an automated email, please do not reply.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private String buildPasswordChangedEmailHtml() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background-color: #10b981; color: white; padding: 20px; text-align: center; }" +
                "        .content { background-color: #f9fafb; padding: 30px; }" +
                "        .warning { background-color: #fee2e2; border-left: 4px solid #ef4444; padding: 15px; margin: 20px 0; }" +
                "        .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 12px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"container\">" +
                "        <div class=\"header\">" +
                "            <h1>✓ Password Changed</h1>" +
                "        </div>" +
                "        <div class=\"content\">" +
                "            <h2>Your Password Has Been Changed</h2>" +
                "            <p>This is a confirmation that your Strux Platform password was successfully changed.</p>" +
                "            <p>If you made this change, no further action is required.</p>" +
                "            <div class=\"warning\">" +
                "                <p style=\"margin: 0;\"><strong>⚠️ Didn't make this change?</strong></p>" +
                "                <p style=\"margin: 5px 0 0 0;\">If you didn't change your password, please contact our support team immediately at support@strux.com</p>" +
                "            </div>" +
                "            <p style=\"margin-top: 30px; color: #6b7280;\">For your security:</p>" +
                "            <ul style=\"color: #6b7280;\">" +
                "                <li>Use a strong, unique password</li>" +
                "                <li>Never share your password with anyone</li>" +
                "                <li>Enable two-factor authentication if available</li>" +
                "            </ul>" +
                "        </div>" +
                "        <div class=\"footer\">" +
                "            <p>© 2024 Strux Platform. All rights reserved.</p>" +
                "            <p>This is an automated email, please do not reply.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private String build2FAEmailHtml(String code) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background-color: #2563eb; color: white; padding: 20px; text-align: center; }" +
                "        .content { background-color: #f9fafb; padding: 30px; }" +
                "        .code-box { background-color: #e0e7ff; border: 2px dashed #2563eb; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 8px; margin: 20px 0; }" +
                "        .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 12px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"container\">" +
                "        <div class=\"header\">" +
                "            <h1>🔒 Verification Code</h1>" +
                "        </div>" +
                "        <div class=\"content\">" +
                "            <h2>Two-Factor Authentication</h2>" +
                "            <p>Use the code below to complete your login:</p>" +
                "            <div class=\"code-box\">" + code + "</div>" +
                "            <p style=\"text-align: center; color: #6b7280;\">This code will expire in 10 minutes</p>" +
                "            <p style=\"margin-top: 30px; color: #6b7280;\">If you didn't request this code, please ignore this email and ensure your account is secure.</p>" +
                "        </div>" +
                "        <div class=\"footer\">" +
                "            <p>© 2024 Strux Platform. All rights reserved.</p>" +
                "            <p>This is an automated email, please do not reply.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 2) {
            return "**@" + domain;
        }

        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
    }
}