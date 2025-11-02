package com.strux.company_service.kafka;

import com.strux.company_service.event.CompanyCreatedEvent;
import com.strux.company_service.event.CompanyDeletedEvent;
import com.strux.company_service.event.CompanyStatusChangedEvent;
import com.strux.company_service.model.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCompanyCreated(Company company) {
        CompanyCreatedEvent event = new CompanyCreatedEvent(
                company.getId(),
                company.getCompanyName(),
                company.getTaxId(),
                LocalDateTime.now()
        );

        kafkaTemplate.send("company.created", company.getId(), event);
        log.info("Published CompanyCreatedEvent: {}", company.getId());
    }

    public void publishCompanyDeleted(String companyId) {
        CompanyDeletedEvent event = new CompanyDeletedEvent(
                companyId,
                LocalDateTime.now()
        );

        kafkaTemplate.send("company.deleted", companyId, event);
        log.info("Published CompanyDeletedEvent: {}", companyId);
    }

}
