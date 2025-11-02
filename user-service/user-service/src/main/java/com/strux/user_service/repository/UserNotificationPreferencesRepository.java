package com.strux.user_service.repository;

import com.strux.user_service.model.UserNotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNotificationPreferencesRepository extends JpaRepository<UserNotificationPreferences,String> {
}
