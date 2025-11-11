package com.strux.user_service.client;

import com.strux.user_service.dto.RegisterRequest;
import com.strux.user_service.dto.RegisterResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    public RegisterResponse registerUser(RegisterRequest request) {
        try {
            log.info("Calling Auth Service to register user: {}", request.getEmail());

            RegisterResponse response = webClientBuilder.build()
                    .post()
                    .uri(authServiceUrl + "/auth/register")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(RegisterResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            log.info("User registered in Auth Service - UserId: {}",
                    response != null ? response.getUserId() : "null");

            return response;

        } catch (Exception e) {
            log.error("Failed to register user in Auth Service: {}", e.getMessage(), e);
            throw new RuntimeException("Auth Service registration failed", e);
        }
    }
}