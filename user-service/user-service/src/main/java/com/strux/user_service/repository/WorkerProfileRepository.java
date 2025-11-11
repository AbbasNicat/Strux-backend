package com.strux.user_service.repository;

import com.strux.user_service.model.WorkerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, String> {
    Optional<WorkerProfile> findByUserId(String userId);
    boolean existsByUserId(String userId);
}
