package com.strux.auth_service.service;

import com.strux.auth_service.exception.InvalidInputException;
import com.strux.auth_service.model.CompanyInvite;
import com.strux.auth_service.repository.CompanyInviteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyInviteService {

    private final CompanyInviteRepository inviteRepository;

    /**
     * Yeni invite code oluştur
     */
    @Transactional
    public String generateInviteCode(String companyId, String createdBy) {
        String code = generateUniqueCode();

        CompanyInvite invite = CompanyInvite.builder()
                .companyId(companyId)
                .inviteCode(code)
                .active(true)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30)) // 30 gün geçerli
                .createdBy(createdBy)
                .usageCount(0)
                .maxUsages(null) // Unlimited
                .build();

        inviteRepository.save(invite);

        log.info("Invite code created: {} for company: {}", code, companyId);
        return code;
    }

    /**
     * Invite code'u doğrula ve company ID döndür
     */
    @Transactional
    public String validateAndUseInviteCode(String code) {
        CompanyInvite invite = inviteRepository.findByInviteCodeAndActiveTrue(code)
                .orElseThrow(() -> new InvalidInputException("Invalid or expired invite code"));

        // Expiry kontrolü
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.setActive(false);
            inviteRepository.save(invite);
            throw new InvalidInputException("Invite code has expired");
        }

        // Max usage kontrolü
        if (invite.getMaxUsages() != null && invite.getUsageCount() >= invite.getMaxUsages()) {
            invite.setActive(false);
            inviteRepository.save(invite);
            throw new InvalidInputException("Invite code has reached maximum usage limit");
        }

        // Usage count artır
        invite.setUsageCount(invite.getUsageCount() + 1);
        inviteRepository.save(invite);

        log.info("Invite code used: {} for company: {}, total usage: {}",
                code, invite.getCompanyId(), invite.getUsageCount());

        return invite.getCompanyId();
    }

    /**
     * Company için aktif invite code'ları getir
     */
    public List<CompanyInvite> getActiveInvitesForCompany(String companyId) {
        return inviteRepository.findByCompanyIdAndActiveTrue(companyId);
    }

    /**
     * Invite code'u iptal et
     */
    @Transactional
    public void deactivateInviteCode(String code) {
        CompanyInvite invite = inviteRepository.findByInviteCodeAndActiveTrue(code)
                .orElseThrow(() -> new InvalidInputException("Invite code not found"));

        invite.setActive(false);
        inviteRepository.save(invite);

        log.info("Invite code deactivated: {}", code);
    }

    /**
     * Süresi dolmuş invite code'ları temizle (her gün 03:00'de çalışır)
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredInvites() {
        List<CompanyInvite> expiredInvites = inviteRepository
                .findByExpiresAtBeforeAndActiveTrue(LocalDateTime.now());

        for (CompanyInvite invite : expiredInvites) {
            invite.setActive(false);
        }

        inviteRepository.saveAll(expiredInvites);
        log.info("Cleaned up {} expired invite codes", expiredInvites.size());
    }

    /**
     * Benzersiz invite code oluştur
     */
    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        do {
            code = RandomStringUtils.randomAlphanumeric(9).toUpperCase();
            attempts++;

            if (attempts > 10) {
                // Çok fazla çakışma varsa daha uzun kod oluştur
                code = RandomStringUtils.randomAlphanumeric(12).toUpperCase();
            }

        } while (inviteRepository.findByInviteCodeAndActiveTrue(code).isPresent());

        return code;
    }
}