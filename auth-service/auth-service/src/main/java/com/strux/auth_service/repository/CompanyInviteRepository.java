package com.strux.auth_service.repository;

import com.strux.auth_service.model.CompanyInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyInviteRepository extends JpaRepository<CompanyInvite, String> {

    Optional<CompanyInvite> findByInviteCodeAndActiveTrue(String inviteCode);

    List<CompanyInvite> findByCompanyIdAndActiveTrue(String companyId);

    List<CompanyInvite> findByExpiresAtBeforeAndActiveTrue(LocalDateTime now);
}