package com.strux.user_service.repository;

import com.strux.user_service.enums.UserRole;
import com.strux.user_service.enums.UserStatus;
import com.strux.user_service.enums.WorkerSpecialty;
import com.strux.user_service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByKeycloakId(String keycloakId);

    Optional<User> findByEmail(String email);

    boolean existsByKeycloakId(String keycloakId);

    boolean existsByEmail(String email);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = """
        SELECT u.* FROM users u 
        LEFT JOIN worker_profiles wp ON u.id = wp.user_id
        WHERE u.role = CAST(:role AS VARCHAR)
        AND u.status = CAST(:status AS VARCHAR)
        AND u.company_id IS NULL
        AND (:specialty IS NULL OR wp.specialty = CAST(:specialty AS VARCHAR))
        AND (:city IS NULL OR LOWER(u.city) = LOWER(:city))
        AND (:isAvailable IS NULL OR u.is_available = :isAvailable)
        AND (wp.rating >= :minRating OR wp.rating IS NULL)
        ORDER BY wp.rating DESC NULLS LAST
        """,
            countQuery = """
        SELECT COUNT(*) FROM users u 
        LEFT JOIN worker_profiles wp ON u.id = wp.user_id
        WHERE u.role = CAST(:role AS VARCHAR)
        AND u.status = CAST(:status AS VARCHAR)
        AND u.company_id IS NULL
        AND (:specialty IS NULL OR wp.specialty = CAST(:specialty AS VARCHAR))
        AND (:city IS NULL OR LOWER(u.city) = LOWER(:city))
        AND (:isAvailable IS NULL OR u.is_available = :isAvailable)
        AND (wp.rating >= :minRating OR wp.rating IS NULL)
        """,
            nativeQuery = true)
    Page<User> searchWorkers(
            @Param("specialty") String specialty,
            @Param("city") String city,
            @Param("isAvailable") Boolean isAvailable,
            @Param("minRating") BigDecimal minRating,
            @Param("role") String role,
            @Param("status") String status,
            Pageable pageable
    );

    Page<User> findByCompanyIdAndRoleAndStatus(
            String companyId,
            UserRole role,
            UserStatus status,
            Pageable pageable
    );
    List<User> findByCompanyIdAndStatus(String companyId, UserStatus status);
    Page<User> findByCompanyIdAndStatus(String companyId, UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.workerProfile wp " +
            "WHERE wp.specialty = :specialty " +
            "AND u.status = :status " +
            "ORDER BY wp.rating DESC")
    Page<User> findTopWorkersBySpecialty(
            @Param("specialty") WorkerSpecialty specialty,
            @Param("status") UserStatus status,
            Pageable pageable
    );
    List<User> findByCompanyIdAndRole(String companyId, UserRole role);
    List<User> findByCompanyIdAndRoleAndStatus(String companyId, UserRole role, UserStatus status);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.workerProfile wp " +
            "WHERE u.isAvailable = true " +
            "AND u.status = :status " +
            "AND (:city IS NULL OR LOWER(u.city) = LOWER(:city)) " +
            "AND (:specialty IS NULL OR wp.specialty = :specialty)")
    Page<User> findAvailableWorkers(
            @Param("city") String city,
            @Param("specialty") WorkerSpecialty specialty,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}