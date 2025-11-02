package com.strux.company_service.kafka;

import com.strux.company_service.event.ProjectCompletedEvent;
import com.strux.company_service.model.Company;
import com.strux.company_service.repository.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CompanyConsumer {

    private final CompanyRepository companyRepository;

    public CompanyConsumer(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }
    // project servisi yazildigi zaman duzelecek
    @KafkaListener(topics = "project.completed", groupId = "company-service")
    public void handleProjectCompleted(ProjectCompletedEvent event) {
        try {
            log.info("Project completed for company: {}", event.getCompanyId());

            Company company = companyRepository.findById(event.getCompanyId())
                    .orElseThrow();

            if (company.getActiveProjects() > 0) {
                company.setActiveProjects(company.getActiveProjects() - 1);
            }

            company.setCompletedProjects(
                    (company.getCompletedProjects() != null ?
                            company.getCompletedProjects() : 0) + 1
            );

            companyRepository.save(company);

            log.info("Updated project counts for company {}: active={}, completed={}",
                    company.getId(), company.getActiveProjects(), company.getCompletedProjects());

        } catch (Exception e) {
            log.error("Error handling ProjectCompletedEvent: {}", e.getMessage(), e);
        }
    }
}
