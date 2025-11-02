package com.strux.user_service.service;

import com.strux.user_service.dto.*;
import com.strux.user_service.enums.AuditEvent;
import com.strux.user_service.enums.UserRole;
import com.strux.user_service.enums.UserStatus;
import com.strux.user_service.event.UserCreatedEvent;
import com.strux.user_service.event.UserDeletedEvent;
import com.strux.user_service.event.UserUpdatedEvent;
import com.strux.user_service.exceptions.*;
import com.strux.user_service.mapper.UserMapper;
import com.strux.user_service.model.User;
import com.strux.user_service.model.WorkerProfile;
import com.strux.user_service.repository.UserNotificationPreferencesRepository;
import com.strux.user_service.repository.UserRatingRepository;
import com.strux.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRatingRepository userRatingRepository;
    private final UserRepository userRepository;
    private final UserNotificationPreferencesRepository userNotificationPreferences;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final FileStorageService fileStorageService;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final String USER_CACHE_PREFIX = "user:";
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/jpg");
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;

    @Transactional
    public UserResponse createUserFromAuthEvent(String keycloakId, String email,
                                                String firstName, String lastName, String phone) {
        try {
            log.info("Creating user from auth event - Email: {}", maskEmail(email));

            if (userRepository.existsByKeycloakId(keycloakId)) {
                log.warn("User already exists - KeycloakId: {}", keycloakId);
                throw new UserAlreadyExistsException("User already exists with this Keycloak ID");
            }

            if (userRepository.existsByEmail(email)) {
                log.warn("Email already in use - Email: {}", maskEmail(email));
                throw new UserAlreadyExistsException("User already exists with this email");
            }

            User user = User.builder()
                    .keycloakId(keycloakId)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .phone(phone)
                    .status(UserStatus.ACTIVE)
                    .twoFaEnabled(false)
                    .provider("LOCAL")
                    .build();

            user = userRepository.save(user);

            auditLogService.logUserEvent(
                    AuditEvent.USER_CREATED,
                    user.getId(),
                    null,
                    "User created from auth service"
            );

            log.info("User created successfully - UserId: {}, Email: {}",
                    user.getId(), maskEmail(email));

            return userMapper.toResponse(user);

        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("User creation error: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to create user", e);
        }
    }

    @Transactional
    public UserResponse registerWorker(WorkerRegistrationRequest request) {

        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .city(request.getCity())
                .companyId(request.getCompanyId())
                .role(UserRole.WORKER)
                .status(UserStatus.PENDING)
                .build();

        WorkerProfile workerProfile = WorkerProfile.builder()
                .specialty(request.getSpecialty())
                .experienceYears(request.getExperienceYears())
                .hourlyRate(request.getHourlyRate())
                .rating(BigDecimal.ZERO)
                .completedTasks(0)
                .totalWorkDays(0)
                .onTimeCompletionCount(0)
                .lateCompletionCount(0)
                .reliabilityScore(BigDecimal.valueOf(100))
                .isAvailable(true)
                .build();

        user.setWorkerProfile(workerProfile);

        User savedUser = userRepository.save(user);

        // 2. Event publish et
        applicationEventPublisher.publishEvent(new UserCreatedEvent(
                savedUser.getId().toString(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                "WORKER"
        ));

        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public UserResponse completeWorkerProfile(UUID userId, WorkerProfileRequest request) {
        User user = userRepository.findById(String.valueOf(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getRole() != UserRole.WORKER) {
            throw new InvalidInputException("User is not a worker");
        }

        WorkerProfile profile = user.getWorkerProfile();
        if (profile == null) {
            profile = new WorkerProfile();
            user.setWorkerProfile(profile);
        }

        profile.setSpecialty(request.getSpecialty());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setHourlyRate(request.getHourlyRate());
        if (request.getAvailableFrom() != null) {
            profile.setAvailableFrom(request.getAvailableFrom());
        }

        if (profile.getRating() == null) profile.setRating(BigDecimal.ZERO);
        if (profile.getCompletedTasks() == null) profile.setCompletedTasks(0);
        if (profile.getReliabilityScore() == null) profile.setReliabilityScore(BigDecimal.valueOf(100));
        if (profile.getIsAvailable() == null) profile.setIsAvailable(true);

        User updatedUser = userRepository.save(user);

        auditLogService.logUserEvent(
                AuditEvent.USER_UPDATED,
                updatedUser.getId(),
                updatedUser.getId().toString(),
                "Worker profile completed - Specialty: " + request.getSpecialty()
        );

        return userMapper.toResponse(updatedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByKeycloakId(String keycloakId) {
        try {
            log.debug("Fetching user by Keycloak ID");

            User user = userRepository.findByKeycloakId(keycloakId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with Keycloak ID"));

            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UserNotFoundException("User is not active");
            }

            return userMapper.toResponse(user);

        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching user by Keycloak ID: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to fetch user", e);
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        try {
            log.debug("Fetching user by ID: {}", userId);

            User user = userRepository.findById(String.valueOf(userId))
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            if (user.getStatus() != UserStatus.ACTIVE) {
                log.warn("Attempt to access inactive user - UserId: {}", userId);
                throw new UserNotFoundException("User is not active");
            }

            return userMapper.toResponse(user);

        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching user: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to fetch user", e);
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        try {
            log.debug("Fetching user by email: {}", maskEmail(email));

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + maskEmail(email)));

            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UserNotFoundException("User is not active");
            }

            return userMapper.toResponse(user);

        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching user by email: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to fetch user", e);
        }
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request, String updatedBy) {
        try {
            log.info("Updating user - UserId: {}", userId);

            User user = userRepository.findById(userId.toString())
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UserNotFoundException("User is not active");
            }

            if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
                user.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null && !request.getLastName().isBlank()) {
                user.setLastName(request.getLastName());
            }
            if (request.getPhone() != null && !request.getPhone().isBlank()) {
                validatePhoneNumber(request.getPhone());
                user.setPhone(request.getPhone());
            }
            if (request.getBio() != null) {
                user.setBio(request.getBio());
            }
            if (request.getPosition() != null) {
                user.setPosition(request.getPosition());
            }
            if (request.getProfileImageUrl() != null) {
                user.setProfileImageUrl(request.getProfileImageUrl());
            }
            if (request.getRole() != null) {
                user.setRole(request.getRole());
            }
            if (request.getCity() != null) {
                user.setCity(request.getCity());
            }


            user = userRepository.save(user);

            try {
                UserUpdatedEvent event = new UserUpdatedEvent(
                        user.getId(),
                        user.getEmail(),
                        LocalDateTime.now()
                );
                kafkaTemplate.send("user-events", event).get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("Failed to publish user updated event: {}", e.getMessage());
            }

            auditLogService.logUserEvent(
                    AuditEvent.USER_UPDATED,
                    user.getId(),
                    updatedBy,
                    "User profile updated"
            );
            log.info("User updated successfully - UserId: {}", userId);

            return userMapper.toResponse(user);

        } catch (UserNotFoundException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("User update error: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to update user", e);
        }
    }

    @Transactional
    public UserResponse uploadAvatar(UUID userId, MultipartFile file, String uploadedBy) {
        try {
            log.info("Uploading avatar - UserId: {}", userId);

            User user = userRepository.findById(userId.toString())
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UserNotFoundException("User is not active");
            }

            validateImageFile(file);

            if (user.getProfileImageUrl() != null) {
                try {
                    fileStorageService.deleteFile(user.getProfileImageUrl());
                } catch (Exception e) {
                    log.warn("Failed to delete old avatar: {}", e.getMessage());
                }
            }

            String avatarUrl = fileStorageService.uploadFile(
                    file,
                    "avatars/" + userId.toString()
            );

            user.setProfileImageUrl(avatarUrl);
            user = userRepository.save(user);

            auditLogService.logUserEvent(
                    AuditEvent.IMAGE_UPLOADED,
                    user.getId(),
                    uploadedBy,
                    "Avatar uploaded"
            );

            log.info("Avatar uploaded successfully - UserId: {}", userId);

            return userMapper.toResponse(user);

        } catch (UserNotFoundException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Avatar upload error: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to upload avatar", e);
        }
    }

    @Transactional
    public void deleteUser(UUID userId, String deletedBy, String reason) {
        try {
            log.info("Deleting user - UserId: {}", userId);

            User user = userRepository.findById(userId.toString())
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            if (user.getDeletedAt() != null) {
                throw new UserAlreadyDeletedException("User is already deleted");
            }

            // Soft delete
            user.setStatus(UserStatus.DELETED);
            user.setDeletedAt(LocalDateTime.now());
            userRepository.save(user);

            try {
                UserDeletedEvent event = new UserDeletedEvent(
                        user.getId().toString(),
                        user.getEmail(),
                        reason,
                        LocalDateTime.now()
                );
                kafkaTemplate.send("user-events", event).get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("Failed to publish user deleted event: {}", e.getMessage());
            }

            auditLogService.logUserEvent(
                    AuditEvent.USER_DELETED,
                    user.getId(),
                    deletedBy,
                    "User deleted: " + reason
            );

            log.info("User deleted successfully - UserId: {}", userId);

        } catch (UserNotFoundException | UserAlreadyDeletedException e) {
            throw e;
        } catch (Exception e) {
            log.error("User deletion error: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to delete user", e);
        }
    }


    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String keyword,
                                          UserStatus status, Pageable pageable) {
        try {
            log.debug("Searching users - Keyword: {}, Status: {}",
                    keyword, status);

            Page<User> users;

            if (keyword != null && !keyword.isEmpty()) {
                users = userRepository.searchByKeyword(keyword, pageable);
            } else if (status != null) {
                users = userRepository.findByStatus(status, pageable);
            } else {
                users = userRepository.findAll(pageable);
            }

            return users.map(userMapper::toResponse);

        } catch (Exception e) {
            log.error("User search error: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to search users", e);
        }
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId) {
        try {
            log.debug("Fetching user profile - UserId: {}", userId);

            User user = userRepository.findById(userId.toString())
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UserNotFoundException("User is not active");
            }

            return userMapper.toProfileResponse(user);

        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching user profile: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to fetch user profile", e);
        }
    }

    @Transactional
    public void markEmailVerified(String keycloakId) {
        try {
            log.info("Marking email as verified - KeycloakId: {}", keycloakId);

            User user = userRepository.findByKeycloakId(keycloakId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            user.setVerifiedAt(LocalDateTime.now());
            userRepository.save(user);

            auditLogService.logUserEvent(
                    AuditEvent.EMAIL_VERIFIED,
                    user.getId(),
                    null,
                    "Email verified"
            );

            log.info("Email verified - UserId: {}", user.getId());

        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Email verification error: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to verify email", e);
        }
    }

    private void validatePhoneNumber(String phone) {
        if (phone != null && !phone.matches("^\\+?[1-9]\\d{1,14}$")) {
            throw new InvalidInputException("Invalid phone number format");
        }
    }


    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidInputException("File is required");
        }

        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new InvalidInputException("File size exceeds 5MB limit");
        }

        String contentType = file.getContentType();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new InvalidInputException("Invalid file type. Allowed: JPEG, PNG, JPG");
        }
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
