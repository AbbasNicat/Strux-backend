package com.strux.user_service.model;

import com.strux.user_service.enums.UserRole;
import com.strux.user_service.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String keycloakId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER; // Default role

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE; // Default status

    private String companyId;

    private String position;

    @Column(length = 500)
    private String bio;

    private String profileImageUrl;

    @Column(name = "two_fa_enabled")
    @Builder.Default
    private Boolean twoFaEnabled = false;

    @Column(name = "provider", length = 50)
    @Builder.Default
    private String provider = "LOCAL";

    @Column(name = "provider_id", length = 255)
    private String providerId;
    @Column(name = "city")
    private String city;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAvailable = false;

    @Embedded
    @Builder.Default
    private WorkerProfile workerProfile = new WorkerProfile(); // Default empty

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean phoneVerified = false;

    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    // Custom builder method
    public static UserBuilder builder() {
        return new UserBuilder()
                .isAvailable(false)
                .emailVerified(false)
                .phoneVerified(false)
                .twoFaEnabled(false)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .provider("LOCAL");
    }
}
