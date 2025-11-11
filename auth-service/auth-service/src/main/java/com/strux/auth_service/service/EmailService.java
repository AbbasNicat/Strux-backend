package com.strux.auth_service.service;

import org.springframework.stereotype.Service;


public interface EmailService {
    void sendEmail(String to, String subject, String body);
    void sendVerificationEmail(String to, String token);
}
