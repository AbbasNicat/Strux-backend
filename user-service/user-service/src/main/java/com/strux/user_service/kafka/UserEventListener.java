package com.strux.user_service.kafka;

import com.strux.user_service.enums.UserRole;
import com.strux.user_service.enums.UserStatus;
import com.strux.user_service.event.UserCreatedEvent;
import com.strux.user_service.event.UserCreatedEventForWorkers;
import com.strux.user_service.event.UserLoggedInEvent;
import com.strux.user_service.event.UserRegisteredEvent;
import com.strux.user_service.model.User;
import com.strux.user_service.model.WorkerProfile;
import com.strux.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final UserRepository userRepository;

    @KafkaListener(
            topics = "user-registered-events",
            groupId = "user-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        try {
            log.info("User registered event received - Email: {}", event.getEmail());

            if (userRepository.existsByKeycloakId(event.getKeycloakId())) {
                log.warn("User already exists - KeycloakId: {}", event.getKeycloakId());
                return;
            }

            User user = User.builder()
                    .keycloakId(event.getKeycloakId())
                    .email(event.getEmail())
                    .firstName(event.getFirstName())
                    .lastName(event.getLastName())
                    .phone(event.getPhone())
                    .role(event.getRole())
                    .status(UserStatus.ACTIVE)
                    .companyId(event.getCompanyId())
                    .position(event.getPosition())
                    .twoFaEnabled(false)
                    .provider("LOCAL")
                    .emailVerified(false)
                    .phoneVerified(false)
                    .build();

            userRepository.save(user);

            log.info("User created successfully from event - UserId: {}, Email: {}",
                    user.getId(), event.getEmail());

        } catch (Exception e) {
            log.error("Error handling user registered event: {}", e.getMessage(), e);
            throw e;
        }
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