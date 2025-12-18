package com.strux.auth_service.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    void sendVerificationEmail(String to, String token);
    void sendPasswordResetEmail(String to, String token);  // ✅ YENİ
    void sendPasswordChangedEmail(String to);              // ✅ YENİ
    void send2FACode(String to, String code);              // ✅ YENİ
}