package com.strux.company_service.kafka;

import com.strux.company_service.enums.EmployeeRole;
import com.strux.company_service.enums.EmployeeStatus;
import com.strux.company_service.event.ProjectCompletedEvent;
import com.strux.company_service.event.WorkerAssignedToProjectEvent;
import com.strux.company_service.event.WorkerRemovedFromCompanyEvent;
import com.strux.company_service.model.Company;
import com.strux.company_service.model.CompanyEmployee;
import com.strux.company_service.repository.CompanyEmployeeRepository;
import com.strux.company_service.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
public class CompanyConsumer {

    private final CompanyRepository companyRepository;
    private final CompanyEmployeeRepository companyEmployeeRepository;

    public CompanyConsumer(CompanyRepository companyRepository, CompanyEmployeeRepository companyEmployeeRepository) {
        this.companyRepository = companyRepository;
        this.companyEmployeeRepository = companyEmployeeRepository;
    }

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

    @KafkaListener(
            topics = "worker.assigned",
            groupId = "company-service",
            containerFactory = "workerAssignedFactory"
    )
    @Transactional
    public void onWorkerAssigned(WorkerAssignedToProjectEvent evt) {
        try {
            if (evt.getCompanyId() == null || evt.getUserId() == null) {
                log.warn("Skip worker.assigned: missing companyId/userId. evt={}", evt);
                return;
            }

            // idempotent kontrol
            if (companyEmployeeRepository.existsByCompanyIdAndUserId(evt.getCompanyId(), evt.getUserId())) {
                log.info("Employee already exists for company {} / user {}", evt.getCompanyId(), evt.getUserId());
                return;
            }

            EmployeeRole role;
            try {
                role = EmployeeRole.valueOf(
                        (evt.getRole() != null ? evt.getRole() : "WORKER").toUpperCase()
                );
            } catch (IllegalArgumentException ex) {
                role = EmployeeRole.WORKER;
            }

            CompanyEmployee ce = CompanyEmployee.builder()
                    .companyId(evt.getCompanyId())
                    .userId(evt.getUserId())
                    .position(evt.getPosition() != null ? evt.getPosition() : "Worker")
                    .role(role)
                    .status(EmployeeStatus.ACTIVE)
                    .department(evt.getDepartment())
                    .hireDate(LocalDate.now())
                    .build();

            companyEmployeeRepository.save(ce);
            log.info("CompanyEmployee inserted for company {} / user {}", evt.getCompanyId(), evt.getUserId());

        } catch (DataIntegrityViolationException dup) {
            log.info("Duplicate employee (idempotent) company={} user={}", evt.getCompanyId(), evt.getUserId());
        } catch (Exception e) {
            log.error("Failed to handle worker.assigned: {}", e.getMessage(), e);
            throw e;
        }
    }
    @KafkaListener(topics = "worker.removed", groupId = "company-service")
    @Transactional
    public void onWorkerRemovedFromCompany(WorkerRemovedFromCompanyEvent event) {
        try {
            log.info("📥 Received worker.removed event: company={}, user={}",
                    event.getCompanyId(), event.getUserId());

            if (event.getCompanyId() == null || event.getUserId() == null) {
                log.warn("Skip worker.removed: missing companyId/userId. event={}", event);
                return;
            }

            int deletedCount = companyEmployeeRepository
                    .deleteByCompanyIdAndUserId(event.getCompanyId(), event.getUserId());

            if (deletedCount > 0) {
                log.info("✅ Deleted {} CompanyEmployee record(s) for company {} / user {}",
                        deletedCount, event.getCompanyId(), event.getUserId());
            } else {
                log.info("ℹ️ No CompanyEmployee record found for company {} / user {} (already removed)",
                        event.getCompanyId(), event.getUserId());
            }

        } catch (Exception e) {
            log.error("❌ Failed to handle worker.removed event: {}", e.getMessage(), e);
            throw e; // Kafka retry için exception fırlat
        }
    }
}
