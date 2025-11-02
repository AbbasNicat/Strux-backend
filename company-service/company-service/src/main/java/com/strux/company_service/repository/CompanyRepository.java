package com.strux.company_service.repository;

import com.strux.company_service.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.strux.company_service.enums.CompanyStatus;
import com.strux.company_service.enums.CompanyType;
import com.strux.company_service.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {

    Optional<Company> findByIdAndDeletedAtIsNull(String id);

    Optional<Company> findByTaxIdAndDeletedAtIsNull(String taxId);

    Optional<Company> findByCompanyNameAndDeletedAtIsNull(String companyName);

    Page<Company> findAllByDeletedAtIsNull(Pageable pageable);

    Page<Company> findByStatusAndDeletedAtIsNull(CompanyStatus status, Pageable pageable);

    List<Company> findByStatusAndDeletedAtIsNull(CompanyStatus status);

    Page<Company> findByTypeAndDeletedAtIsNull(CompanyType type, Pageable pageable);

    List<Company> findByIsVerifiedTrueAndStatusAndDeletedAtIsNull(CompanyStatus status);

    boolean existsByTaxIdAndDeletedAtIsNull(String taxId);

    boolean existsByCompanyNameAndDeletedAtIsNull(String companyName);

    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL AND " +
            "(LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.taxId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Company> searchCompanies(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL AND c.ownerId = :ownerId")
    List<Company> findByOwnerId(@Param("ownerId") String ownerId);

    @Query("SELECT COUNT(c) FROM Company c WHERE c.deletedAt IS NULL AND c.status = :status")
    long countByStatus(@Param("status") CompanyStatus status);
}
