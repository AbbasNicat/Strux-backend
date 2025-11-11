package com.strux.project_service.repository;

import com.strux.project_service.enums.ProjectStatus;
import com.strux.project_service.enums.ProjectType;
import com.strux.project_service.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    Optional<Project> findByName(String name);

    List<Project> findByCompanyId(String companyId);

    /**
     * Company ID ve project ID ile proje getirir (güvenlik için - ÖNEMLİ!)
     */
    Optional<Project> findByIdAndCompanyId(String projectId, String companyId);

    /**
     * Belirli bir harita alanındaki (bounds) projeleri getir
     */
    @Query("SELECT p FROM Project p " +
            "WHERE p.location.latitude IS NOT NULL " +
            "AND p.location.latitude BETWEEN :swLat AND :neLat " +
            "AND p.location.longitude BETWEEN :swLng AND :neLng")
    List<Project> findByLocationBounds(
            @Param("swLat") Double southWestLat,
            @Param("swLng") Double southWestLng,
            @Param("neLat") Double northEastLat,
            @Param("neLng") Double northEastLng
    );

    /**
     * Company ID ve bounds ile projeleri filtreler (GÜVENLİK İÇİN)
     */
    @Query("SELECT p FROM Project p WHERE p.companyId = :companyId " +
            "AND p.location.latitude IS NOT NULL " +
            "AND p.location.latitude BETWEEN :swLat AND :neLat " +
            "AND p.location.longitude BETWEEN :swLng AND :neLng")
    List<Project> findByCompanyIdAndLocationWithinBounds(
            @Param("companyId") String companyId,
            @Param("swLat") Double southWestLat,
            @Param("swLng") Double southWestLng,
            @Param("neLat") Double northEastLat,
            @Param("neLng") Double northEastLng
    );

    /**
     * Yakındaki projeleri bul (Haversine formülü ile)
     */
    @Query(value = """
    SELECT * FROM projects p 
    WHERE p.latitude IS NOT NULL 
      AND p.longitude IS NOT NULL 
      AND (
        6371 * acos(
          cos(radians(:lat)) * cos(radians(p.latitude)) * cos(radians(p.longitude) - radians(:lng)) 
          + sin(radians(:lat)) * sin(radians(p.latitude))
        )
      ) <= :radiusKm
    """, nativeQuery = true)
    List<Project> findNearbyProjects(
            @Param("lat") Double latitude,
            @Param("lng") Double longitude,
            @Param("radiusKm") Double radiusKm
    );

    /**
     * Belirli bir lokasyona yakın projeleri getirir (company bazlı - GÜVENLİK İÇİN)
     */
    @Query(value = "SELECT * FROM projects p WHERE p.company_id = :companyId " +
            "AND p.latitude IS NOT NULL AND p.longitude IS NOT NULL " +
            "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(p.latitude)) * " +
            "cos(radians(p.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(p.latitude)))) <= :radiusKm",
            nativeQuery = true)
    List<Project> findNearbyProjectsByCompany(
            @Param("companyId") String companyId,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm
    );

    /**
     * Şehre göre projeleri getir
     */
    @Query("SELECT p FROM Project p WHERE LOWER(p.location.city) = LOWER(:city)")
    List<Project> findByLocationCity(@Param("city") String city);

    /**
     * Şehre göre projeleri getir (company bazlı)
     */
    @Query("SELECT p FROM Project p WHERE p.companyId = :companyId AND LOWER(p.location.city) = LOWER(:city)")
    List<Project> findByCompanyIdAndLocationCity(@Param("companyId") String companyId, @Param("city") String city);

    /**
     * Bölgeye göre projeleri getir
     */
    @Query("SELECT p FROM Project p WHERE LOWER(p.location.district) = LOWER(:district)")
    List<Project> findByLocationDistrict(@Param("district") String district);

    /**
     * Qarabağ bölgesindeki projeleri getir
     */
    @Query("SELECT p FROM Project p " +
            "WHERE LOWER(p.location.city) IN ('fuzuli', 'shusha', 'khankendi', 'aghdam', 'jabrayil', 'zangilan', 'gubadli', 'lachin', 'kalbajar') " +
            "OR LOWER(p.location.district) LIKE '%qarabag%' " +
            "OR LOWER(p.location.district) LIKE '%karabakh%'")
    List<Project> findQarabagProjects();

    /**
     * Qarabağ bölgesindeki projeleri getir (company bazlı)
     */
    @Query("SELECT p FROM Project p " +
            "WHERE p.companyId = :companyId AND (" +
            "LOWER(p.location.city) IN ('fuzuli', 'shusha', 'khankendi', 'aghdam', 'jabrayil', 'zangilan', 'gubadli', 'lachin', 'kalbajar') " +
            "OR LOWER(p.location.district) LIKE '%qarabag%' " +
            "OR LOWER(p.location.district) LIKE '%karabakh%')")
    List<Project> findQarabagProjectsByCompany(@Param("companyId") String companyId);

    /**
     * Place ID'ye göre proje bul
     */
    @Query("SELECT p FROM Project p WHERE p.location.placeId = :placeId")
    Optional<Project> findByLocationPlaceId(@Param("placeId") String placeId);

    /**
     * Şirkete göre aktif projeleri getir
     */
    @Query("SELECT p FROM Project p " +
            "WHERE p.companyId = :companyId " +
            "AND p.status = 'IN_PROGRESS'")
    List<Project> findActiveProjectsByCompanyId(@Param("companyId") String companyId);

    /**
     * Aktif projeleri getir
     */
    @Query("SELECT p FROM Project p WHERE p.status = 'IN_PROGRESS'")
    List<Project> findActiveProjects();

    /**
     * Tamamlanmış projeleri getir
     */
    @Query("SELECT p FROM Project p WHERE p.status = 'COMPLETED'")
    List<Project> findCompletedProjects();

    /**
     * Geciken projeleri getir
     */
    @Query("SELECT p FROM Project p " +
            "WHERE p.plannedEndDate < :currentDate " +
            "AND p.status = 'IN_PROGRESS'")
    List<Project> findDelayedProjects(@Param("currentDate") LocalDate currentDate);

    /**
     * Geciken projeleri getir (company bazlı)
     */
    @Query("SELECT p FROM Project p " +
            "WHERE p.companyId = :companyId " +
            "AND p.plannedEndDate < :currentDate " +
            "AND p.status = 'IN_PROGRESS'")
    List<Project> findDelayedProjectsByCompany(@Param("companyId") String companyId, @Param("currentDate") LocalDate currentDate);

    /**
     * İsim veya açıklamaya göre arama
     */
    @Query("SELECT p FROM Project p " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Project> searchByNameOrDescription(@Param("keyword") String keyword);

    /**
     * İsim veya açıklamaya göre arama (company bazlı)
     */
    @Query("SELECT p FROM Project p " +
            "WHERE p.companyId = :companyId AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Project> searchByNameOrDescriptionAndCompany(@Param("companyId") String companyId, @Param("keyword") String keyword);

    /**
     * Global arama
     */
    @Query("SELECT p FROM Project p " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.location.address) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.location.city) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Project> globalSearch(@Param("keyword") String keyword);

    /**
     * Global arama (company bazlı)
     */
    @Query("SELECT p FROM Project p " +
            "WHERE p.companyId = :companyId AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.location.address) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.location.city) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Project> globalSearchByCompany(@Param("companyId") String companyId, @Param("keyword") String keyword);

    /**
     * En son oluşturulan projeleri getir
     */
    @Query("SELECT p FROM Project p ORDER BY p.createdAt DESC")
    List<Project> findAllOrderByCreatedAtDesc();

    /**
     * En son oluşturulan projeleri getir (company bazlı)
     */
    @Query("SELECT p FROM Project p WHERE p.companyId = :companyId ORDER BY p.createdAt DESC")
    List<Project> findByCompanyIdOrderByCreatedAtDesc(@Param("companyId") String companyId);

    /**
     * Çoklu filtre ile arama
     */
    @Query("SELECT p FROM Project p WHERE " +
            "(:statuses IS NULL OR p.status IN :statuses) AND " +
            "(:types IS NULL OR p.type IN :types) AND " +
            "(:companyIds IS NULL OR p.companyId IN :companyIds) AND " +
            "(:minCompletion IS NULL OR p.overallProgress >= :minCompletion) AND " +
            "(:maxCompletion IS NULL OR p.overallProgress <= :maxCompletion)")
    List<Project> findByFilters(
            @Param("statuses") List<ProjectStatus> statuses,
            @Param("types") List<ProjectType> types,
            @Param("companyIds") List<String> companyIds,
            @Param("minCompletion") BigDecimal minCompletion,
            @Param("maxCompletion") BigDecimal maxCompletion
    );

    /**
     * Çoklu filtreleme (company, statuses, types - GÜVENLİK İÇİN)
     */
    @Query("SELECT p FROM Project p WHERE p.companyId = :companyId " +
            "AND (:statuses IS NULL OR p.status IN :statuses) " +
            "AND (:types IS NULL OR p.type IN :types) " +
            "AND (:minCompletion IS NULL OR p.overallProgress >= :minCompletion) " +
            "AND (:maxCompletion IS NULL OR p.overallProgress <= :maxCompletion)")
    List<Project> findByCompanyIdAndFilters(
            @Param("companyId") String companyId,
            @Param("statuses") List<ProjectStatus> statuses,
            @Param("types") List<ProjectType> types,
            @Param("minCompletion") BigDecimal minCompletion,
            @Param("maxCompletion") BigDecimal maxCompletion
    );

    /**
     * Location bilgisi olan projeleri getirir (map için - company bazlı)
     */
    @Query("SELECT p FROM Project p WHERE p.companyId = :companyId " +
            "AND p.location.latitude IS NOT NULL AND p.location.longitude IS NOT NULL")
    List<Project> findByCompanyIdWithLocation(@Param("companyId") String companyId);

    /**
     * Şirkete ait proje sayısı
     */
    Long countByCompanyId(String companyId);

    /**
     * Proje var mı kontrol et
     */
    boolean existsByCompanyIdAndName(String companyId, String name);
}