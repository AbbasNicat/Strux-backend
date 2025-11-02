package com.strux.notification_service.config;

import com.strux.notification_service.enums.NotificationType;
import com.strux.notification_service.model.NotificationTemplate;
import com.strux.notification_service.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationTemplateSeeder implements CommandLineRunner {

    private final NotificationTemplateRepository templateRepository;

    @Override
    public void run(String... args) {
        log.info("Checking notification templates...");

        // issue.assigned templates
        seedTemplate("issue.assigned", NotificationType.IN_APP,
                "Sizə yeni məsələ təyin edildi",
                "Issue #{{issueId}} sizə təyin edildi");

        seedTemplate("issue.assigned", NotificationType.EMAIL,
                "Yeni məsələ təyini",
                "Salam! Issue #{{issueId}} sizə təyin edildi. Şirkət: {{companyId}}");

        seedTemplate("issue.assigned", NotificationType.PUSH,
                "Yeni məsələ",
                "Sizə yeni bir məsələ təyin edildi");

        // issue.created templates
        seedTemplate("issue.created", NotificationType.IN_APP,
                "Issue yaradıldı",
                "Yeni issue: {{title}}");

        seedTemplate("issue.created", NotificationType.EMAIL,
                "Yeni Issue",
                "{{title}} adlı yeni issue yaradıldı. ID: {{issueId}}");

        // issue.updated templates
        seedTemplate("issue.updated", NotificationType.IN_APP,
                "Issue yeniləndi",
                "Issue #{{issueId}} yeniləndi");

        // issue.resolved templates
        seedTemplate("issue.resolved", NotificationType.IN_APP,
                "Issue həll edildi",
                "Issue #{{issueId}} həll edildi");

        seedTemplate("issue.resolved", NotificationType.EMAIL,
                "Issue Həll Edildi",
                "Sizin açdığınız issue #{{issueId}} həll edildi");

        // issue.closed templates
        seedTemplate("issue.closed", NotificationType.IN_APP,
                "Issue bağlandı",
                "Issue #{{issueId}} bağlandı");

        // user.registered templates
        seedTemplate("user.registered", NotificationType.EMAIL,
                "Xoş gəldiniz",
                "Hesabınız uğurla yaradıldı. Xoş gəldiniz!");

        seedTemplate("user.registered", NotificationType.IN_APP,
                "Xoş gəldiniz",
                "Hesabınız yaradıldı");

        // task.completed templates
        seedTemplate("task.completed", NotificationType.IN_APP,
                "Task tamamlandı",
                "Task tamamlandı");

        seedTemplate("task.completed", NotificationType.PUSH,
                "Task tamamlandı",
                "Bir task tamamlandı");

        // project.budget.exceeded templates
        seedTemplate("project.budget.exceeded", NotificationType.EMAIL,
                "Büdcə Aşıldı",
                "Layihə büdcəsi limiti aşıldı. Zəhmət olmasa nəzərdən keçirin.");

        seedTemplate("project.budget.exceeded", NotificationType.PUSH,
                "Büdcə Aşıldı",
                "Layihə büdcəsi aşıldı");

        seedTemplate("project.budget.exceeded", NotificationType.IN_APP,
                "Büdcə Aşıldı",
                "Layihə büdcəsi limiti aşıldı");

        log.info("Template seeding completed");
    }

    private void seedTemplate(String eventType, NotificationType type,
                              String subject, String body) {
        if (templateRepository.findByEventTypeAndType(eventType, type).isEmpty()) {
            NotificationTemplate template = NotificationTemplate.builder()
                    .id(UUID.randomUUID().toString())
                    .eventType(eventType)
                    .type(type)
                    .language("az")
                    .subject(subject)
                    .body(body)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            templateRepository.save(template);
            log.info("✓ Created template: {} - {}", eventType, type);
        }
    }
}