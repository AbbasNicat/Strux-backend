package com.strux.company_service.service;

import com.strux.company_service.dto.CompanyRequest;
import com.strux.company_service.dto.CompanyResponse;
import com.strux.company_service.dto.CompanyUpdateRequest;
import com.strux.company_service.enums.CompanyStatus;
import com.strux.company_service.enums.CompanyType;
import com.strux.company_service.exceptions.CompanyAlreadyExistsException;
import com.strux.company_service.exceptions.CompanyNotFoundException;
import com.strux.company_service.exceptions.CompanyServiceException;
import com.strux.company_service.exceptions.InvalidInputException;
import com.strux.company_service.kafka.CompanyEventProducer;
import com.strux.company_service.mapper.CompanyMapper;
import com.strux.company_service.model.Company;
import com.strux.company_service.repository.CompanyRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
// company olusturma direkt bizde yeni adminde olacaq ona gore statusu direkt active olacaq
@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final FileStorageService fileStorageService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CompanyEventProducer companyEventProducer;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        try {
            validateCompanyRequest(request);
            checkDuplicates(request);

            Company company = companyMapper.toEntity(request);
            company.setId(UUID.randomUUID().toString());
            company.setStatus(CompanyStatus.ACTIVE);
            company.setIsVerified(true);
            company.setVerifiedAt(LocalDateTime.now());
            company.setActiveProjects(0);
            company.setCompletedProjects(0);
            company.setCreatedAt(LocalDateTime.now());
            company.setUpdatedAt(LocalDateTime.now());

            entityManager.persist(company);
            entityManager.flush();

            try {
                companyEventProducer.publishCompanyCreated(company);
            } catch (Exception e) {
                log.warn("Failed to publish company created event: {}", e.getMessage());
            }

            log.info("Company created successfully - ID: {}", company.getId());
            return companyMapper.toResponse(company);

        } catch (CompanyAlreadyExistsException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating company: {}", e.getMessage(), e);
            throw new CompanyServiceException("Failed to create company", e);
        }
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(String companyId) {
        try {

            Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                    .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

            return companyMapper.toResponse(company);

        } catch (CompanyNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching company: {}", e.getMessage(), e);
            throw new CompanyServiceException("Failed to fetch company", e);
        }
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyByTaxId(String taxId) {
        try {
            Company company = companyRepository.findByTaxIdAndDeletedAtIsNull(taxId)
                    .orElseThrow(() -> new CompanyNotFoundException("Company not found with Tax ID: " + taxId));

            return companyMapper.toResponse(company);

        } catch (CompanyNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching company by tax ID: {}", e.getMessage(), e);
            throw new CompanyServiceException("Failed to fetch company", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAllCompanies(Pageable pageable) {
        try {

            Page<Company> companies = companyRepository.findAllByDeletedAtIsNull(pageable);
            return companies.map(companyMapper::toResponse);

        } catch (Exception e) {
            log.error("Error fetching companies: {}", e.getMessage(), e);
            throw new CompanyServiceException("Failed to fetch companies", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> getCompaniesByStatus(CompanyStatus status, Pageable pageable) {
        try {

            Page<Company> companies = companyRepository.findByStatusAndDeletedAtIsNull(status, pageable);
            return companies.map(companyMapper::toResponse);

        } catch (Exception e) {
            log.error("Error fetching companies by status: {}", e.getMessage(), e);
            throw new CompanyServiceException("Failed to fetch companies by status", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> getCompaniesByType(CompanyType type, Pageable pageable) {
        try {

            Page<Company> companies = companyRepository.findByTypeAndDeletedAtIsNull(type, pageable);
            return companies.map(companyMapper::toResponse);

        } catch (Exception e) {
            log.error("Error fetching companies by type: {}", e.getMessage(), e);
            throw new CompanyServiceException("Failed to fetch companies by type", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> searchCompanies(String keyword, Pageable pageable) {
        try {

            Page<Company> companies = companyRepository.searchCompanies(keyword, pageable);
            return companies.map(companyMapper::toResponse);

        } catch (Exception e) {
            log.error("Error searching companies: {}", e.getMessage(), e);
            throw new CompanyServiceException("Failed to search companies", e);
        }
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> getActiveCompanies() {
        try {

            List<Company> companies = companyRepository.findByStatusAndDeletedAtIsNull(CompanyStatus.ACTIVE);
            return companies.stream()
                    .map(companyMapper::toResponse)
                    .toList();

        } catch (Exception e) {
            log.error("Error fetching active companies: {}", e.getMessage(), e);
            throw new CompanyServiceException("Failed to fetch active companies", e);
        }
    }

    @Transactional
    public CompanyResponse updateCompany(String companyId, CompanyUpdateRequest request) {
        try {

            Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                    .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

            updateCompanyFields(company, request);

            company = companyRepository.save(company);

            log.info("Company updated successfully - ID: {}", companyId);
            return companyMapper.toResponse(company);

        } catch (CompanyNotFoundException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            throw new CompanyServiceException("Failed to update company", e);
        }
    }

    @Transactional
    public CompanyResponse uploadCompanyLogo(String companyId, MultipartFile file) {
        try {

            Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                    .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

            String logoUrl = fileStorageService.uploadFile(file, "company-logos");
            company.setLogoUrl(logoUrl);

            company = companyRepository.save(company);

            return companyMapper.toResponse(company);

        } catch (CompanyNotFoundException e) {
            throw e;
        } catch (Exception e) {

            throw new CompanyServiceException("Failed to upload company logo", e);
        }
    }

    @Transactional
    public void updateCompanyStatus(String companyId, CompanyStatus status) {
        try {

            Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                    .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

            company.setStatus(status);
            companyRepository.save(company);

        } catch (CompanyNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CompanyServiceException("Failed to update company status", e);
        }
    }

    @Transactional
    public void incrementProjectCount(String companyId, boolean isActive) {
        try {
            Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                    .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

            if (isActive) {
                company.setActiveProjects(company.getActiveProjects() != null
                        ? company.getActiveProjects() + 1 : 1);
            } else {
                company.setCompletedProjects(company.getCompletedProjects() != null
                        ? company.getCompletedProjects() + 1 : 1);
                if (company.getActiveProjects() != null && company.getActiveProjects() > 0) {
                    company.setActiveProjects(company.getActiveProjects() - 1);
                }
            }

            companyRepository.save(company);

        } catch (CompanyNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CompanyServiceException("Failed to update project count", e);
        }
    }

    @Transactional
    public void deleteCompany(String companyId) {
        try {

            Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                    .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

            company.setDeletedAt(LocalDateTime.now());
            company.setStatus(CompanyStatus.INACTIVE);

            companyRepository.save(company);
            companyEventProducer.publishCompanyDeleted(companyId);

        } catch (CompanyNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CompanyServiceException("Failed to delete company", e);
        }
    }

    private void validateCompanyRequest(CompanyRequest request) {
        if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
            throw new InvalidInputException("Company name is required");
        }
        if (request.getTaxId() == null || request.getTaxId().isBlank()) {
            throw new InvalidInputException("Tax ID is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new InvalidInputException("Email is required");
        }
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new InvalidInputException("Phone is required");
        }
    }

    private void checkDuplicates(CompanyRequest request) {
        if (companyRepository.existsByTaxIdAndDeletedAtIsNull(request.getTaxId())) {
            throw new CompanyAlreadyExistsException("Company with this Tax ID already exists");
        }
        if (companyRepository.existsByCompanyNameAndDeletedAtIsNull(request.getCompanyName())) {
            throw new CompanyAlreadyExistsException("Company with this name already exists");
        }
    }

    private void updateCompanyFields(Company company, CompanyUpdateRequest request) {
        if (request.getCompanyName() != null) {
            company.setCompanyName(request.getCompanyName());
        }
        if (request.getDescription() != null) {
            company.setDescription(request.getDescription());
        }
        if (request.getEmail() != null) {
            company.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            company.setPhone(request.getPhone());
        }
        if (request.getWebsite() != null) {
            company.setWebsite(request.getWebsite());
        }
        if (request.getAddress() != null) {
            company.setAddress(request.getAddress());
        }
        if (request.getEmployeeCount() != null) {
            company.setEmployeeCount(request.getEmployeeCount());
        }
        if (request.getFinancials() != null) {
            company.setFinancials(request.getFinancials());
        }
        if (request.getLicense() != null) {
            company.setLicense(request.getLicense());
        }
    }
}
