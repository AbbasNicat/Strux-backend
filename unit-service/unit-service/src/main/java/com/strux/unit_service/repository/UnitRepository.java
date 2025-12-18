package com.strux.unit_service.repository;

import com.strux.unit_service.enums.*;
import com.strux.unit_service.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, String> {

    boolean existsByProjectIdAndUnitNumber(String projectId, String unitNumber);

    // ✅ FIX: Floor schema'ları hariç tut
    @Query("SELECT u FROM Unit u WHERE u.parentUnitId = :parentUnitId AND (u.notes IS NULL OR u.notes NOT LIKE '%FLOOR_PLAN_TEMPLATE%') AND u.deletedAt IS NULL")
    List<Unit> findActualSubUnits(@Param("parentUnitId") String parentUnitId);

    // ✅ Floor schema'ları getir
    @Query("SELECT u FROM Unit u WHERE u.parentUnitId = :parentUnitId AND u.notes LIKE '%FLOOR_PLAN_TEMPLATE%' AND u.deletedAt IS NULL ORDER BY u.floor ASC")
    List<Unit> findFloorSchemas(@Param("parentUnitId") String parentUnitId);

    boolean existsByProjectIdAndUnitNumberAndDeletedAtIsNull(String projectId, String unitNumber);
    boolean existsByProjectIdAndUnitNumberAndIdNot(String projectId, String unitNumber, String id);

    List<Unit> findByCompanyIdAndDeletedAtIsNull(String companyId);

    // ✅ FIX: Project altındaki gerçek unit'leri getir (floor schema'lar hariç)
    @Query("SELECT u FROM Unit u WHERE u.projectId = :projectId AND (u.notes IS NULL OR u.notes NOT LIKE '%FLOOR_PLAN_TEMPLATE%') AND u.deletedAt IS NULL")
    List<Unit> findActualUnitsByProject(@Param("projectId") String projectId);

    // Eski metod - deprecated olarak işaretle
    @Deprecated
    List<Unit> findByProjectIdAndDeletedAtIsNull(String projectId);

    // ✅ FIX: Building altındaki gerçek unit'leri getir
    @Query("SELECT u FROM Unit u WHERE u.buildingId = :buildingId AND (u.notes IS NULL OR u.notes NOT LIKE '%FLOOR_PLAN_TEMPLATE%') AND u.deletedAt IS NULL")
    List<Unit> findActualUnitsByBuilding(@Param("buildingId") String buildingId);

    @Deprecated
    List<Unit> findByBuildingIdAndDeletedAtIsNull(String buildingId);

    Optional<Unit> findByUnitNumberAndProjectIdAndDeletedAtIsNull(String unitNumber, String projectId);

    // ✅ FIX: Floor schema'ları hariç tut
    @Query("SELECT u FROM Unit u WHERE u.blockName = :blockName AND u.floor = :floor AND (u.notes IS NULL OR u.notes NOT LIKE '%FLOOR_PLAN_TEMPLATE%') AND u.deletedAt IS NULL")
    List<Unit> findActualUnitsByBlockAndFloor(@Param("blockName") String blockName, @Param("floor") Integer floor);

    List<Unit> findByBlockNameAndFloorAndDeletedAtIsNull(String blockName, Integer floor);

    // ✅ FIX: Floor schema'ları hariç tut
    @Query("SELECT u FROM Unit u WHERE u.projectId = :projectId AND u.blockName = :blockName AND (u.notes IS NULL OR u.notes NOT LIKE '%FLOOR_PLAN_TEMPLATE%') AND u.deletedAt IS NULL")
    List<Unit> findActualUnitsByProjectAndBlock(@Param("projectId") String projectId, @Param("blockName") String blockName);

    List<Unit> findByProjectIdAndBlockNameAndDeletedAtIsNull(String projectId, String blockName);

    // ✅ FIX: Floor schema'ları hariç tut
    @Query("SELECT u FROM Unit u WHERE u.projectId = :projectId AND u.floor = :floor AND (u.notes IS NULL OR u.notes NOT LIKE '%FLOOR_PLAN_TEMPLATE%') AND u.deletedAt IS NULL")
    List<Unit> findActualUnitsByProjectAndFloor(@Param("projectId") String projectId, @Param("floor") Integer floor);

    List<Unit> findByProjectIdAndFloorAndDeletedAtIsNull(String projectId, Integer floor);

    List<Unit> findByTypeAndDeletedAtIsNull(UnitType type);

    List<Unit> findByProjectIdAndTypeAndDeletedAtIsNull(String projectId, UnitType type);

    List<Unit> findByStatusAndDeletedAtIsNull(UnitStatus status);

    // ✅ FIX: Floor schema'ları hariç tut
    @Query("SELECT u FROM Unit u WHERE u.projectId = :projectId AND u.status = :status AND (u.notes IS NULL OR u.notes NOT LIKE '%FLOOR_PLAN_TEMPLATE%') AND u.deletedAt IS NULL")
    List<Unit> findActualUnitsByProjectAndStatus(@Param("projectId") String projectId, @Param("status") UnitStatus status);

    List<Unit> findByProjectIdAndStatusAndDeletedAtIsNull(String projectId, UnitStatus status);

    List<Unit> findBySaleStatusAndDeletedAtIsNull(SaleStatus saleStatus);

    // ✅ FIX: Floor schema'ları hariç tut
    @Query("SELECT u FROM Unit u WHERE u.projectId = :projectId AND u.saleStatus = :saleStatus AND (u.notes IS NULL OR u.notes NOT LIKE '%FLOOR_PLAN_TEMPLATE%') AND u.deletedAt IS NULL")
    List<Unit> findActualUnitsByProjectAndSaleStatus(@Param("projectId") String projectId, @Param("saleStatus") SaleStatus saleStatus);

    List<Unit> findByProjectIdAndSaleStatusAndDeletedAtIsNull(String projectId, SaleStatus saleStatus);

    List<Unit> findByCurrentPhaseAndDeletedAtIsNull(ConstructionPhase phase);

    List<Unit> findByProjectIdAndCurrentPhaseAndDeletedAtIsNull(String projectId, ConstructionPhase phase);

    List<Unit> findByOwnerIdAndDeletedAtIsNull(String ownerId);

    List<Unit> findByGrossAreaBetweenAndDeletedAtIsNull(BigDecimal minArea, BigDecimal maxArea);

    List<Unit> findByNetAreaBetweenAndDeletedAtIsNull(BigDecimal minArea, BigDecimal maxArea);

    List<Unit> findByListPriceBetweenAndDeletedAtIsNull(BigDecimal minPrice, BigDecimal maxPrice);

    List<Unit> findBySalePriceBetweenAndDeletedAtIsNull(BigDecimal minPrice, BigDecimal maxPrice);

    List<Unit> findByRoomCountAndDeletedAtIsNull(Integer roomCount);

    List<Unit> findByRoomCountBetweenAndDeletedAtIsNull(Integer minRooms, Integer maxRooms);

    List<Unit> findByDirectionAndDeletedAtIsNull(Direction direction);

    @Query("SELECT u FROM Unit u WHERE :feature MEMBER OF u.features AND u.deletedAt IS NULL")
    List<Unit> findByFeature(@Param("feature") String feature);

    @Query("SELECT u FROM Unit u WHERE :tag MEMBER OF u.tags AND u.deletedAt IS NULL")
    List<Unit> findByTag(@Param("tag") String tag);

    List<Unit> findByCompletionPercentageGreaterThanEqualAndDeletedAtIsNull(Integer minPercentage);

    List<Unit> findByCompletionPercentageBetweenAndDeletedAtIsNull(Integer min, Integer max);

    List<Unit> findByHasGardenAndDeletedAtIsNull(Boolean hasGarden);

    List<Unit> findByHasTerraceAndDeletedAtIsNull(Boolean hasTerrace);

    List<Unit> findByIsSmartHomeAndDeletedAtIsNull(Boolean isSmartHome);

    List<Unit> findByHasParkingSpaceAndDeletedAtIsNull(Boolean hasParkingSpace);

    @Query("SELECT u FROM Unit u WHERE u.estimatedCompletionDate < :now AND u.status NOT IN ('COMPLETED', 'DELIVERED') AND u.deletedAt IS NULL")
    List<Unit> findOverdueUnits(@Param("now") LocalDateTime now);

    List<Unit> findByProjectIdAndEstimatedCompletionDateBeforeAndStatusNotInAndDeletedAtIsNull(
            String projectId,
            LocalDateTime date,
            List<UnitStatus> statuses
    );

    // ✅ Parent unit'leri getir (building/block level)
    @Query("SELECT u FROM Unit u WHERE u.projectId = :projectId AND u.parentUnitId IS NULL AND (u.notes IS NULL OR u.notes NOT LIKE '%FLOOR_PLAN_TEMPLATE%') AND u.deletedAt IS NULL")
    List<Unit> findActualUnitsByProjectAndParentIsNull(@Param("projectId") String projectId);

    @Query("SELECT u FROM Unit u WHERE (LOWER(u.unitNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.unitName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND u.deletedAt IS NULL")
    List<Unit> searchByKeyword(@Param("keyword") String keyword);



    // ✅ FIX: Floor schema'ları sayma
    @Query("SELECT COUNT(u) FROM Unit u WHERE u.projectId = :projectId AND (u.notes IS NULL OR u.notes NOT LIKE '%FLOOR_PLAN_TEMPLATE%') AND u.deletedAt IS NULL")
    Long countActualUnitsByProject(@Param("projectId") String projectId);

    Long countByProjectIdAndDeletedAtIsNull(String projectId);

    Long countByProjectIdAndStatusAndDeletedAtIsNull(String projectId, UnitStatus status);

    Long countByProjectIdAndSaleStatusAndDeletedAtIsNull(String projectId, SaleStatus saleStatus);

    Long countByProjectIdAndTypeAndDeletedAtIsNull(String projectId, UnitType type);

    Long countByProjectIdAndCurrentPhaseAndDeletedAtIsNull(String projectId, ConstructionPhase phase);

    @Query("SELECT u.type, COUNT(u) FROM Unit u WHERE u.projectId = :projectId AND u.deletedAt IS NULL GROUP BY u.type")
    List<Object[]> countByTypeGrouped(@Param("projectId") String projectId);

    @Query("SELECT u.status, COUNT(u) FROM Unit u WHERE u.projectId = :projectId AND u.deletedAt IS NULL GROUP BY u.status")
    List<Object[]> countByStatusGrouped(@Param("projectId") String projectId);

    @Query("SELECT u.saleStatus, COUNT(u) FROM Unit u WHERE u.projectId = :projectId AND u.deletedAt IS NULL GROUP BY u.saleStatus")
    List<Object[]> countBySaleStatusGrouped(@Param("projectId") String projectId);

    @Query("SELECT u.currentPhase, COUNT(u) FROM Unit u WHERE u.projectId = :projectId AND u.deletedAt IS NULL GROUP BY u.currentPhase")
    List<Object[]> countByPhaseGrouped(@Param("projectId") String projectId);

    @Query("SELECT AVG(u.completionPercentage) FROM Unit u WHERE u.projectId = :projectId AND u.deletedAt IS NULL")
    Double getAverageCompletionPercentage(@Param("projectId") String projectId);

    @Query("SELECT SUM(u.grossArea) FROM Unit u WHERE u.projectId = :projectId AND u.deletedAt IS NULL")
    Double getTotalGrossArea(@Param("projectId") String projectId);

    @Query("SELECT SUM(u.netArea) FROM Unit u WHERE u.projectId = :projectId AND u.deletedAt IS NULL")
    Double getTotalNetArea(@Param("projectId") String projectId);

    @Query("SELECT SUM(u.salePrice) FROM Unit u WHERE u.projectId = :projectId AND u.saleStatus = 'SOLD' AND u.deletedAt IS NULL")
    BigDecimal getTotalSalesValue(@Param("projectId") String projectId);

    @Query("SELECT SUM(u.totalPaid) FROM Unit u WHERE u.projectId = :projectId AND u.deletedAt IS NULL")
    BigDecimal getTotalCollectedPayments(@Param("projectId") String projectId);

    @Query("SELECT SUM(u.remainingPayment) FROM Unit u WHERE u.projectId = :projectId AND u.deletedAt IS NULL")
    BigDecimal getTotalRemainingPayments(@Param("projectId") String projectId);

    @Query("SELECT AVG(u.qualityScore) FROM Unit u WHERE u.projectId = :projectId AND u.qualityScore IS NOT NULL AND u.deletedAt IS NULL")
    Integer getAverageQualityScore(@Param("projectId") String projectId);

    boolean existsByIdAndDeletedAtIsNull(String id);

    boolean existsByUnitNumberAndProjectIdAndDeletedAtIsNull(String unitNumber, String projectId);

    List<Unit> findByProjectId(String projectId);
}