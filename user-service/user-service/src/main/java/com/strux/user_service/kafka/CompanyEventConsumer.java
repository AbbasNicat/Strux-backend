package com.strux.user_service.kafka;

import com.strux.user_service.event.CompanyCreatedEvent;
import com.strux.user_service.event.CompanyDeletedEvent;
import com.strux.user_service.enums.UserStatus;
import com.strux.user_service.model.User;
import com.strux.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyEventConsumer {

    private final UserRepository userRepository;

    @KafkaListener(topics = "company.deleted", groupId = "user-service")
    public void handleCompanyDeleted(CompanyDeletedEvent event) {
        try {
            log.info("Received CompanyDeletedEvent: {}", event.companyId());

            List<User> companyUsers = userRepository
                    .findByCompanyIdAndStatus(event.companyId(), UserStatus.ACTIVE);

            companyUsers.forEach(user -> {
                user.setStatus(UserStatus.INACTIVE);
                user.setCompanyId(null);
            });

            userRepository.saveAll(companyUsers);

            log.info("Deactivated {} users for deleted company {}",
                    companyUsers.size(), event.companyId());

        } catch (Exception e) {
            log.error("Error handling CompanyDeletedEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "company.created", groupId = "user-service")
    public void handleCompanyCreated(CompanyCreatedEvent event) {
        log.info("Company created notification: {}", event.companyName());

    }
}
