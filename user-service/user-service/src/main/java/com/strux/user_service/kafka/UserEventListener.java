package com.strux.user_service.kafka;

import com.strux.user_service.dto.WorkerProfileTemp;
import com.strux.user_service.enums.AuditEvent;
import com.strux.user_service.enums.UserRole;
import com.strux.user_service.enums.UserStatus;
import com.strux.user_service.enums.WorkerSpecialty;
import com.strux.user_service.event.UserCreatedEvent;
import com.strux.user_service.event.UserCreatedEventForWorkers;
import com.strux.user_service.event.UserLoggedInEvent;
import com.strux.user_service.event.UserRegisteredEvent;
import com.strux.user_service.exceptions.UserServiceException;
import com.strux.user_service.model.User;
import com.strux.user_service.model.WorkerProfile;
import com.strux.user_service.repository.UserRepository;
import com.strux.user_service.repository.WorkerProfileRepository;
import com.strux.user_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final WorkerProfileRepository workerProfileRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(
            topics = "user-registered-events",
            groupId = "user-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("📥 User registration event received - KeycloakId: {}, Role: {}, Email: {}",
                event.getKeycloakId(), event.getRole(), event.getEmail());

        // === 1️⃣ User upsert ===
        User user = userRepository.findByKeycloakId(event.getKeycloakId())
                .orElseGet(() -> User.builder()
                        .keycloakId(event.getKeycloakId())
                        .email(event.getEmail())
                        .firstName(event.getFirstName())
                        .lastName(event.getLastName())
                        .phone(event.getPhone())
                        .role(event.getRole())
                        .companyId(event.getCompanyId())
                        .position(event.getPosition())
                        .city(event.getCity())
                        .bio(event.getBio())
                        .status(UserStatus.ACTIVE)
                        .isAvailable(event.getRole() == UserRole.WORKER)
                        .build());

        user = userRepository.save(user);

        // === 2️⃣ WorkerProfile idempotent create/update ===
        if (event.getRole() == UserRole.WORKER && event.getWorkerProfile() != null) {
            var wpData = event.getWorkerProfile();

            WorkerProfile profile = workerProfileRepository.findById(user.getId()).orElse(null);

            if (profile == null) {
                profile = new WorkerProfile();
                profile.setUser(user); // ✅ @MapsId sayesinde PK otomatik kopyalanır
            }

            profile.setSpecialty(wpData.getSpecialty());
            profile.setExperienceYears(wpData.getExperienceYears() != null ? wpData.getExperienceYears() : 0);
            profile.setHourlyRate(wpData.getHourlyRate() != null ? wpData.getHourlyRate() : BigDecimal.ZERO);
            if (profile.getRating() == null) profile.setRating(BigDecimal.ZERO);
            if (profile.getReliabilityScore() == null) profile.setReliabilityScore(BigDecimal.valueOf(100.0));
            if (profile.getTotalWorkDays() == null) profile.setTotalWorkDays(0);
            if (profile.getCompletedTasks() == null) profile.setCompletedTasks(0);
            if (profile.getLateCompletionCount() == null) profile.setLateCompletionCount(0);
            if (profile.getOnTimeCompletionCount() == null) profile.setOnTimeCompletionCount(0);
            if (profile.getIsAvailable() == null) profile.setIsAvailable(true);

            workerProfileRepository.save(profile);
            user.setWorkerProfile(profile);
        }

        // === 3️⃣ Audit log ===
        auditLogService.logUserEvent(
                AuditEvent.USER_CREATED,
                user.getId(),
                event.getKeycloakId(),
                "User created from registration event - Role: " + event.getRole()
        );

        log.info("✅ User saved successfully: {}, role: {}", user.getEmail(), user.getRole());
    }


    @EventListener
    @Transactional
    public void handleUserCreatedEvent(UserCreatedEventForWorkers event) {
        try {
            log.info("Creating user in user service: {}", event.getEmail());

            User user = User.builder()
                    .email(event.getEmail())
                    .firstName(event.getFirstName())
                    .lastName(event.getLastName())
                    .keycloakId(event.getKeycloakId())
                    .role(UserRole.valueOf(event.getRole()))
                    .status(UserStatus.ACTIVE)
                    .build();

            if (user.getRole() == UserRole.WORKER) {
                WorkerProfile workerProfile = WorkerProfile.builder()
                        .isAvailable(true)
                        .rating(BigDecimal.ZERO)
                        .completedTasks(0)
                        .reliabilityScore(BigDecimal.valueOf(100))
                        .build();
                user.setWorkerProfile(workerProfile);
            }

            User savedUser = userRepository.save(user);

            log.info("User created successfully in user service: {}", savedUser.getId());

        } catch (Exception e) {
            log.error("Failed to create user in user service: {}", event.getEmail(), e);
        }
    }

    /**
     * Handles user login events from auth-service
     * Listens to: user-logged-in-events topic
     */
    @KafkaListener(
            topics = "user-logged-in-events",
            groupId = "user-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleUserLoggedInEvent(UserLoggedInEvent event) {
        try {
            log.info("User logged in event received - UserId: {}", event.getUserId());

            userRepository.findByKeycloakId(event.getUserId())
                    .ifPresent(user -> {
                        user.setLastLoginAt(event.getLoginTime());
                        userRepository.save(user);
                        log.info("Last login updated - UserId: {}", user.getId());
                    });

        } catch (Exception e) {
            log.error("Error handling user logged in event: {}", e.getMessage(), e);
        }
    }
}