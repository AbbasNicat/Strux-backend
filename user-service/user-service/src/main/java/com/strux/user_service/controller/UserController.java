package com.strux.user_service.controller;

import com.strux.user_service.dto.UpdateUserRequest;
import com.strux.user_service.dto.UserProfileResponse;
import com.strux.user_service.dto.UserResponse;
import com.strux.user_service.dto.WorkerProfileRequest;
import com.strux.user_service.enums.UserStatus;
import com.strux.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;


    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        log.info("GET /api/users/{}", userId);
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsersByIds(@RequestParam List<String> ids) {
        List<UserResponse> users = userService.getUsersByIds(ids);
        return ResponseEntity.ok(users);
    }


    @PostMapping("/{userId}/worker-profile")
    public ResponseEntity<UserResponse> completeWorkerProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody WorkerProfileRequest request,
            @RequestHeader("Authorization") String token) {

        log.info("POST /api/users/{}/worker-profile", userId);

        UserResponse response = userService.completeWorkerProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/keycloak/{keycloakId}")
    public ResponseEntity<UserResponse> getUserByKeycloakId(@PathVariable String keycloakId) {
        log.info("GET /api/users/keycloak/{}", keycloakId);
        UserResponse response = userService.getUserByKeycloakId(keycloakId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        log.info("GET /api/users/email/{}", email);
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable UUID userId) {
        log.info("GET /api/users/{}/profile", userId);
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String updatedBy) {

        log.info("PUT /api/users/{} - Request: {}", userId, request);
        log.info("City value from request: {}", request.getCity());

        UserResponse response = userService.updateUser(userId, request, updatedBy);

        log.info("Response City: {}", response.getCity());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/avatar")
    public ResponseEntity<UserResponse> uploadAvatar(
            @PathVariable UUID userId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) String uploadedBy) {
        log.info("POST /api/users/{}/avatar", userId);
        UserResponse response = userService.uploadAvatar(userId, file, uploadedBy);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId,
            @RequestParam(required = false, defaultValue = "User requested deletion") String reason,
            @RequestHeader(value = "X-User-Id", required = false) String deletedBy) {
        log.info("DELETE /api/users/{}", userId);
        userService.deleteUser(userId, deletedBy, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            Pageable pageable) {
        log.info("GET /api/users/search - keyword: {}, status: {}",
                keyword, status);
        Page<UserResponse> response = userService.searchUsers(keyword, status, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/internal/verify-email/{keycloakId}")
    public ResponseEntity<Void> markEmailVerified(@PathVariable String keycloakId) {
        log.info("POST /api/users/internal/verify-email/{}", keycloakId);
        userService.markEmailVerified(keycloakId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/internal/create")
    public ResponseEntity<UserResponse> createUserFromAuthEvent(
            @RequestParam String keycloakId,
            @RequestParam String email,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam(required = false) String phone) {
        log.info("POST /api/users/internal/create - email: {}", email);
        UserResponse response = userService.createUserFromAuthEvent(
                keycloakId, email, firstName, lastName, phone);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
