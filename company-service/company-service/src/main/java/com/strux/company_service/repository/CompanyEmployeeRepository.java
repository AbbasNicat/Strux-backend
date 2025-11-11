package com.strux.company_service.repository;


import com.strux.company_service.model.CompanyEmployee;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyEmployeeRepository extends JpaRepository<CompanyEmployee, String> {
    boolean existsByCompanyIdAndUserId(String companyId, String userId);
    List<CompanyEmployee> findByCompanyId(String companyId);
    @Modifying
    @Transactional
    @Query("DELETE FROM CompanyEmployee ce WHERE ce.companyId = :companyId AND ce.userId = :userId")
    int deleteByCompanyIdAndUserId(@Param("companyId") String companyId, @Param("userId") String userId);
}
