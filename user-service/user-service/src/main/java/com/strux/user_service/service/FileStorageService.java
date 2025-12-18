package com.strux.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final WebClient webClient;

    @Value("${document.service.url:http://localhost:9089}")
    private String documentServiceUrl;

    public String uploadFile(MultipartFile file, String subDirectory) throws IOException {
        try {
            log.info("📤 Uploading file to Document Service (MinIO)");
            log.info("   File: {}, Size: {} bytes", file.getOriginalFilename(), file.getSize());

            String userId = subDirectory.replace("avatars/", "");

            // ✅ Basit multipart data - @RequestParam'lar için
            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            // File part
            builder.part("file", file.getResource())
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "form-data; name=file; filename=" + file.getOriginalFilename());

            // Request parameters as simple parts
            builder.part("companyId", "system");
            builder.part("entityType", "USER");
            builder.part("entityId", userId);
            builder.part("documentType", "AVATAR");
            builder.part("category", "PROFILE");
            builder.part("description", "User avatar");

            // Get JWT token
            String authToken = extractAuthToken();

            // ✅ Yeni avatar endpoint'e istek
            var requestSpec = webClient
                    .post()
                    .uri(documentServiceUrl + "/api/documents/avatar/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .header("X-User-Id", userId);

            if (authToken != null && !authToken.isEmpty()) {
                requestSpec = requestSpec.header("Authorization", "Bearer " + authToken);
                log.info("🔐 Authorization header added");
            }

            Map<String, Object> response = requestSpec
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("fileUrl")) {
                String fileUrl = (String) response.get("fileUrl");
                log.info("✅ File uploaded successfully: {}", fileUrl);
                return fileUrl;
            }

            throw new IOException("Failed to get file URL from Document Service");

        } catch (Exception e) {
            log.error("❌ File upload failed: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    private String extractAuthToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                return ((Jwt) authentication.getPrincipal()).getTokenValue();
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not extract auth token: {}", e.getMessage());
        }
        return null;
    }

    public void deleteFile(String fileUrl) throws IOException {
        try {
            log.info("🗑️ Attempting to delete file from MinIO: {}", fileUrl);
            log.info("ℹ️ File deletion from MinIO not implemented yet");
        } catch (Exception e) {
            log.error("❌ File deletion failed: {}", e.getMessage(), e);
        }
    }
}