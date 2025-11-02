package com.strux.user_service.mapper;

import com.strux.user_service.dto.UserResponse;
import com.strux.user_service.dto.UserProfileResponse;
import com.strux.user_service.dto.WorkerProfileResponse;
import com.strux.user_service.model.User;
import com.strux.user_service.model.WorkerProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse.UserResponseBuilder responseBuilder = UserResponse.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .profileImageUrl(user.getProfileImageUrl())
                .bio(user.getBio())
                .role(user.getRole())
                .city(user.getCity())
                .companyId(user.getCompanyId())
                .position(user.getPosition())
                .status(user.getStatus())
                .twoFaEnabled(user.getTwoFaEnabled())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .verifiedAt(user.getVerifiedAt());

        if (user.getWorkerProfile() != null) {
            responseBuilder.workerProfile(toWorkerProfileResponse(user.getWorkerProfile()));
        }

        return responseBuilder.build();
    }

    public UserProfileResponse toProfileResponse(User user) {
        if (user == null) {
            return null;
        }

        UserProfileResponse.UserProfileResponseBuilder responseBuilder = UserProfileResponse.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .profileImageUrl(user.getProfileImageUrl())
                .bio(user.getBio())
                .role(user.getRole())
                .companyId(user.getCompanyId())
                .position(user.getPosition())
                .status(user.getStatus())
                .twoFaEnabled(user.getTwoFaEnabled())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .verifiedAt(user.getVerifiedAt());

        if (user.getWorkerProfile() != null) {
            responseBuilder.workerProfile(toWorkerProfileResponse(user.getWorkerProfile()));
        }

        return responseBuilder.build();
    }

    public WorkerProfileResponse toWorkerProfileResponse(WorkerProfile profile) {
        if (profile == null) {
            return null;
        }

        return WorkerProfileResponse.builder()
                .specialty(profile.getSpecialty())
                .experienceYears(profile.getExperienceYears())
                .hourlyRate(profile.getHourlyRate())
                .rating(profile.getRating())
                .completedTasks(profile.getCompletedTasks())
                .totalWorkDays(profile.getTotalWorkDays())
                .onTimeCompletionCount(profile.getOnTimeCompletionCount())
                .lateCompletionCount(profile.getLateCompletionCount())
                .reliabilityScore(profile.getReliabilityScore())
                .activeProjectIds(profile.getActiveProjectIds())
                .isAvailable(profile.getIsAvailable())
                .availableFrom(profile.getAvailableFrom())
                .build();
    }

}