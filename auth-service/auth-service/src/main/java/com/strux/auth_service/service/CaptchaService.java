package com.strux.auth_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaptchaService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${recaptcha.secret-key:}")
    private String recaptchaSecret;

    public boolean verifyCaptcha(String captchaToken) {
        if (captchaToken == null || captchaToken.isEmpty()) {
            return false;
        }

        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("secret", recaptchaSecret);
            formData.add("response", captchaToken);

            String response = webClient.post()
                    .uri("https://www.google.com/recaptcha/api/siteverify")
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(java.time.Duration.ofSeconds(5))
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            boolean success = jsonNode.get("success").asBoolean();

            if (success) {
                double score = jsonNode.has("score") ? jsonNode.get("score").asDouble() : 1.0;
                return score >= 0.5; // Threshold for reCAPTCHA v3
            }

            return false;
        } catch (Exception e) {
            log.error("CAPTCHA verification failed: {}", e.getMessage());
            return false;
        }
    }
}
