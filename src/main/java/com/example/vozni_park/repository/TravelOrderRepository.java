package com.example.vozni_park.repository;

import com.example.vozni_park.entity.TravelOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TravelOrderRepository extends JpaRepository<TravelOrder, Long> {

    // Custom query methods
    Optional<TravelOrder> findByWorkOrderNumber(String workOrderNumber);

    Optional<TravelOrder> findByLocation_IdLocationUnitAndTravelOrderNumber(Long locationId, String travelOrderNumber);

    List<TravelOrder> findByStatus(String status);

    List<TravelOrder> findByLocation_IdLocationUnit(Long locationId);

    List<TravelOrder> findByCreatedByUserId(Long userId);

    // Find travel orders by date range
    @Query("SELECT t FROM TravelOrder t WHERE t.dateFrom >= :startDate AND t.dateTo <= :endDate")
    List<TravelOrder> findByDateRange(LocalDate startDate, LocalDate endDate);

    // Find active travel orders for a location
    @Query("SELECT t FROM TravelOrder t WHERE t.location.idLocationUnit = :locationId AND t.status = 'IN_PROGRESS'")
    List<TravelOrder> findActiveByLocation(@Param("locationId") Long locationId);

    // Find travel orders by driver
    @Query("SELECT t FROM TravelOrder t JOIN t.driverTravelOrders dto WHERE dto.driver.idDriver = :driverId")
    List<TravelOrder> findByDriverId(Long driverId);

    // Find travel orders by vehicle
    @Query("SELECT t FROM TravelOrder t JOIN t.travelOrderVehicles tov WHERE tov.vehicle.idVehicle = :vehicleId")
    List<TravelOrder> findByVehicleId(Long vehicleId);

    /**
     * Find travel orders by multiple location IDs
     */
    @Query("SELECT to FROM TravelOrder to " +
            "WHERE to.location.idLocationUnit IN :locationIds")
    List<TravelOrder> findByLocationIds(@Param("locationIds") List<Long> locationIds);

    /**
     * Find travel orders by location IDs and status
     */
    @Query("SELECT to FROM TravelOrder to " +
            "WHERE to.location.idLocationUnit IN :locationIds " +
            "AND to.status = :status")
    List<TravelOrder> findByLocationIdsAndStatus(@Param("locationIds") List<Long> locationIds,
                                                 @Param("status") String status);

    /**
     * Find active travel orders by location IDs
     */
    @Query("SELECT to FROM TravelOrder to " +
            "WHERE to.location.idLocationUnit IN :locationIds " +
            "AND to.status = 'IN_PROGRESS'")
    List<TravelOrder> findActiveByLocationIds(@Param("locationIds") List<Long> locationIds);

    /**
     * Find travel orders by location IDs and date range
     */
    @Query("SELECT to FROM TravelOrder to " +
            "WHERE to.location.idLocationUnit IN :locationIds " +
            "AND to.dateFrom >= :startDate AND to.dateTo <= :endDate")
    List<TravelOrder> findByLocationIdsAndDateRange(@Param("locationIds") List<Long> locationIds,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    // ✨ NEW: Optimized driver query with location filtering
    /**
     * Find travel orders by driver ID and location IDs
     */
    @Query("SELECT DISTINCT to FROM TravelOrder to " +
            "JOIN to.driverTravelOrders dto " +
            "WHERE dto.driver.idDriver = :driverId " +
            "AND to.location.idLocationUnit IN :locationIds")
    List<TravelOrder> findByDriverIdAndLocationIds(@Param("driverId") Long driverId,
                                                   @Param("locationIds") List<Long> locationIds);

    // ✨ NEW: Optimized vehicle query with location filtering
    /**
     * Find travel orders by vehicle ID and location IDs
     */
    @Query("SELECT DISTINCT to FROM TravelOrder to " +
            "JOIN to.travelOrderVehicles tov " +
            "WHERE tov.vehicle.idVehicle = :vehicleId " +
            "AND to.location.idLocationUnit IN :locationIds")
    List<TravelOrder> findByVehicleIdAndLocationIds(@Param("vehicleId") Long vehicleId,
                                                    @Param("locationIds") List<Long> locationIds);

    boolean existsByWorkOrderNumber(String workOrderNumber);

    /**
     * Returns true if the vehicle has any ACTIVE travel order (status_code = 1).
     * Used by status calc: Priority 4 for vehicles (ON_DUTY).
     */
    @Query("SELECT COUNT(tov) > 0 FROM TravelOrderVehicle tov " +
            "WHERE tov.vehicle.idVehicle = :vehicleId " +
            "AND tov.travelOrder.statusCode = 1")
    boolean hasActiveVehicleTravelOrder(@Param("vehicleId") Long vehicleId);

    /**
     * Returns true if the vehicle has any UNFINISHED travel order (status_code = 2).
     * Used by status calc: Priority 3 for vehicles (UNAVAILABLE).
     */
    @Query("SELECT COUNT(tov) > 0 FROM TravelOrderVehicle tov " +
            "WHERE tov.vehicle.idVehicle = :vehicleId " +
            "AND tov.travelOrder.statusCode = 2")
    boolean hasUnfinishedVehicleTravelOrder(@Param("vehicleId") Long vehicleId);

    /**
     * Returns true if the driver has any ACTIVE travel order (status_code = 1).
     * Used by status calc: Priority 3 for drivers (ON_DUTY).
     */
    @Query("SELECT COUNT(dto) > 0 FROM DriverTravelOrder dto " +
            "WHERE dto.driver.idDriver = :driverId " +
            "AND dto.travelOrder.statusCode = 1")
    boolean hasActiveDriverTravelOrder(@Param("driverId") Long driverId);

    /**
     * Bulk fetch: all ACTIVE travel orders whose date_to has passed.
     * Used by the daily status refresh job to transition ACTIVE → UNFINISHED.
     */
    @Query("SELECT t FROM TravelOrder t " +
            "WHERE t.statusCode = 1 " +
            "AND t.dateTo < :today " +
            "AND t.endingMileage IS NULL")
    List<TravelOrder> findExpiredActiveTravelOrders(@Param("today") LocalDate today);
}