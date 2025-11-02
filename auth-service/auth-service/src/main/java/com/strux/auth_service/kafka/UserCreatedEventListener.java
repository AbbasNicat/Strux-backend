package com.strux.auth_service.kafka;

import com.strux.auth_service.client.UserServiceClient;
import com.strux.auth_service.dto.UpdateKeycloakIdRequest;
import com.strux.auth_service.event.UserCreatedEvent;
import com.strux.auth_service.service.KeycloakAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedEventListener {

    private final KeycloakAdminService keycloakService;
    private final UserServiceClient userServiceClient;

    @EventListener
    @Async
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        try {
            log.info("Processing user creation event for: {}", event.getEmail());

            String keycloakId = keycloakService.createUser(
                    event.getEmail(),
                    event.getPassword(),
                    event.getFirstName(),
                    event.getLastName(),
                    event.getRole()
            );

            UpdateKeycloakIdRequest request = new UpdateKeycloakIdRequest();
            request.setKeycloakId(keycloakId);

            userServiceClient.updateKeycloakId(event.getUserId(), request);

            log.info("Successfully created user in Keycloak: {} with keycloakId: {}",
                    event.getEmail(), keycloakId);

        } catch (Exception e) {
            log.error("Failed to process user creation event for: {}", event.getEmail(), e);
        }
    }
}
