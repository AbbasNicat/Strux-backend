package com.strux.notification_service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials-path:firebase-credentials.json}")
    private String credentialsPath;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        try {
            // Firebase'i başlat
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount;

                try {
                    // Önce classpath'den dene (resources/)
                    serviceAccount = new ClassPathResource(credentialsPath).getInputStream();
                } catch (Exception e) {
                    // Yoksa dosya sisteminden dene
                    serviceAccount = new FileInputStream(credentialsPath);
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase initialized successfully");
            }

            return FirebaseMessaging.getInstance();

        } catch (Exception e) {
            log.error("❌ Failed to initialize Firebase: {}", e.getMessage(), e);
            throw e;
        }
    }
}
