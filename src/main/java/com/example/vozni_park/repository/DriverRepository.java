package com.example.vozni_park.repository;

import com.example.vozni_park.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    // Custom query methods
    Optional<Driver> findBySapNumber(Long sapNumber);

    List<Driver> findByStatus(String status);

    List<Driver> findByFullNameContainingIgnoreCase(String name);

    // Find drivers by location
    @Query("SELECT d FROM Driver d JOIN d.driverLocation dl WHERE dl.locationUnit.idLocationUnit = :locationId")
    List<Driver> findByLocationId(Long locationId);

    // Find drivers without location
    @Query("SELECT d FROM Driver d WHERE d.driverLocation IS NULL")
    List<Driver> findDriversWithoutLocation();

    // Find available drivers (active status, not on travel order)
    @Query("SELECT d FROM Driver d WHERE d.status = 'Active' AND d.idDriver NOT IN " +
            "(SELECT dto.driver.idDriver FROM DriverTravelOrder dto WHERE dto.travelOrder.status = 'IN_PROGRESS')")
    List<Driver> findAvailableDrivers();

    /**
     * Find drivers by multiple location IDs
     */
    @Query("SELECT DISTINCT d FROM Driver d " +
            "LEFT JOIN d.driverLocation dl " +
            "WHERE dl.locationUnit.idLocationUnit IN :locationIds")
    List<Driver> findByLocationIds(@Param("locationIds") List<Long> locationIds);

    /**
     * Find drivers by location IDs with specific status
     */
    @Query("SELECT DISTINCT d FROM Driver d " +
            "LEFT JOIN d.driverLocation dl " +
            "WHERE dl.locationUnit.idLocationUnit IN :locationIds " +
            "AND d.status = :status")
    List<Driver> findByLocationIdsAndStatus(@Param("locationIds") List<Long> locationIds,
                                            @Param("status") String status);

    /**
     * Find available drivers by location IDs (active, not on travel order)
     */
    @Query("SELECT DISTINCT d FROM Driver d " +
            "LEFT JOIN d.driverLocation dl " +
            "LEFT JOIN d.driverTravelOrders dto " +
            "LEFT JOIN dto.travelOrder to " +
            "WHERE dl.locationUnit.idLocationUnit IN :locationIds " +
            "AND d.status = 'Active' " +
            "AND (to.status IS NULL OR to.status != 'IN_PROGRESS')")
    List<Driver> findAvailableDriversByLocationIds(@Param("locationIds") List<Long> locationIds);

    /**
     * Find drivers by location IDs and name search
     */
    @Query("SELECT DISTINCT d FROM Driver d " +
            "LEFT JOIN d.driverLocation dl " +
            "WHERE dl.locationUnit.idLocationUnit IN :locationIds " +
            "AND LOWER(d.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Driver> findByLocationIdsAndNameContaining(@Param("locationIds") List<Long> locationIds,
                                                    @Param("name") String name);

    boolean existsBySapNumber(Long sapNumber);
}