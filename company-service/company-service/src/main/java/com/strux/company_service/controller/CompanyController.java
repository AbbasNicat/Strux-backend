package com.strux.company_service.controller;

import com.strux.company_service.dto.CompanyEmployeeResponse;
import com.strux.company_service.dto.CompanyRequest;
import com.strux.company_service.dto.CompanyResponse;
import com.strux.company_service.dto.CompanyUpdateRequest;
import com.strux.company_service.enums.CompanyStatus;
import com.strux.company_service.enums.CompanyType;
import com.strux.company_service.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/{companyId}/employees")
    public ResponseEntity<List<CompanyEmployeeResponse>> getCompanyEmployees(
            @PathVariable String companyId) {
        log.debug("Fetching employees for company {}", companyId);
        List<CompanyEmployeeResponse> employees = companyService.getCompanyEmployees(companyId);
        return ResponseEntity.ok(employees);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest request) {
        log.info("Creating Company {}", request.getCompanyName());
        CompanyResponse companyResponse = companyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(companyResponse);
    }

    @PutMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> updateCompany(@PathVariable String companyId, @Valid @RequestBody CompanyUpdateRequest request) {
        log.info("Updating Company {}", request.getCompanyName());
        CompanyResponse companyResponse = companyService.updateCompany(companyId,request);
        return ResponseEntity.status(HttpStatus.OK).body(companyResponse);
    }

    @PatchMapping("/{companyId}/status")
    public ResponseEntity<Void> updateCompanyStatus(
            @PathVariable String companyId,
            @RequestParam CompanyStatus status
    ) {
        log.info("Updating company company id {} and status {}", companyId, status);
        companyService.updateCompanyStatus(companyId, status);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable String companyId) {
        log.debug("Fetching company {}", companyId);
        CompanyResponse response = companyService.getCompanyById(companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tax-id/{taxId}")
    public ResponseEntity<CompanyResponse> getCompanyByTaxId(@PathVariable String taxId) {
        log.debug("Fetching company by tax ID {}", taxId);
        CompanyResponse response = companyService.getCompanyByTaxId(taxId);
        return ResponseEntity.ok(response);
    }



    @GetMapping
    public ResponseEntity<Page<CompanyResponse>> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<CompanyResponse> response = companyService.getAllCompanies(pageable);
        return ResponseEntity.ok(response);

    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<CompanyResponse>> getCompaniesByStatus(
            @PathVariable CompanyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.debug("Fetching companies by status {}", status);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CompanyResponse> response = companyService.getCompaniesByStatus(status, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Page<CompanyResponse>> getCompaniesByType(
            @PathVariable CompanyType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.debug("Fetching companies by type {}", type);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CompanyResponse> response = companyService.getCompaniesByType(type, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CompanyResponse>> searchCompanies(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Searching companies for {}", keyword);

        Pageable pageable = PageRequest.of(page, size);
        Page<CompanyResponse> response = companyService.searchCompanies(keyword, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CompanyResponse>> getActiveCompanies() {
        log.debug("Fetching active companies");
        List<CompanyResponse> response = companyService.getActiveCompanies();
        return ResponseEntity.ok(response);
    }


    @PostMapping(value = "/{companyId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompanyResponse> uploadCompanyLogo(
            @PathVariable String companyId,
            @RequestParam("file") MultipartFile file
    ) {
        log.info("Uploading company logo {}", companyId);

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        CompanyResponse response = companyService.uploadCompanyLogo(companyId, file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> deleteCompany(@PathVariable String companyId) {
        log.info("Soft deleting company {}", companyId);
        companyService.deleteCompany(companyId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{companyId}/projects/increment")
    public ResponseEntity<Void> incrementProjectCount(
            @PathVariable String companyId,
            @RequestParam boolean isActive
    ) {
        log.debug("Incrementing project count for {}, {} ", companyId, isActive);
        companyService.incrementProjectCount(companyId, isActive);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Company Service is running!");
    }
}

