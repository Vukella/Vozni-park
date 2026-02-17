package com.example.vozni_park.repository;

import com.example.vozni_park.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    // Custom query methods
    Optional<Vehicle> findBySapNumber(Long sapNumber);
    
    Optional<Vehicle> findByChassisNumber(String chassisNumber);
    
    List<Vehicle> findByVehicleStatus(String vehicleStatus);
    
    List<Vehicle> findByVehicleModelId(Long vehicleModelId);
    
    List<Vehicle> findByFuelTypeId(Long fuelTypeId);

    /**
     * Find vehicles by multiple location IDs (for LOCAL_ADMIN with multiple locations)
     */
    @Query("SELECT DISTINCT v FROM Vehicle v " +
            "LEFT JOIN v.vehicleLocation vl " +
            "WHERE vl.locationUnit.idLocationUnit IN :locationIds")
    List<Vehicle> findByLocationIds(@Param("locationIds") List<Long> locationIds);

    /**
     * Find vehicles by location IDs with specific status
     */
    @Query("SELECT DISTINCT v FROM Vehicle v " +
            "LEFT JOIN v.vehicleLocation vl " +
            "WHERE vl.locationUnit.idLocationUnit IN :locationIds " +
            "AND v.vehicleStatus = :status")
    List<Vehicle> findByLocationIdsAndStatus(@Param("locationIds") List<Long> locationIds,
                                             @Param("status") String status);

    /**
     * Find vehicles by location IDs with specific model
     */
    @Query("SELECT DISTINCT v FROM Vehicle v " +
            "LEFT JOIN v.vehicleLocation vl " +
            "WHERE vl.locationUnit.idLocationUnit IN :locationIds " +
            "AND v.vehicleModel.idVehicleModel = :modelId")
    List<Vehicle> findByLocationIdsAndModel(@Param("locationIds") List<Long> locationIds,
                                            @Param("modelId") Long modelId);

    /**
     * Find vehicles by location IDs with specific fuel type
     */
    @Query("SELECT DISTINCT v FROM Vehicle v " +
            "LEFT JOIN v.vehicleLocation vl " +
            "WHERE vl.locationUnit.idLocationUnit IN :locationIds " +
            "AND v.fuelType.idFuelType = :fuelTypeId")
    List<Vehicle> findByLocationIdsAndFuelType(@Param("locationIds") List<Long> locationIds,
                                               @Param("fuelTypeId") Long fuelTypeId);
    
    // Find vehicles by location
    @Query("SELECT v FROM Vehicle v JOIN v.vehicleLocation vl WHERE vl.locationUnit.idLocationUnit = :locationId")
    List<Vehicle> findByLocationId(Long locationId);
    
    // Find vehicles without location
    @Query("SELECT v FROM Vehicle v WHERE v.vehicleLocation IS NULL")
    List<Vehicle> findVehiclesWithoutLocation();
    
    boolean existsBySapNumber(Long sapNumber);
    
    boolean existsByChassisNumber(String chassisNumber);

    /**
     * Find available vehicles (Active status, not currently IN_PROGRESS in a travel order)
     */
    @Query("SELECT DISTINCT v FROM Vehicle v " +
            "LEFT JOIN v.vehicleLocation vl " +
            "LEFT JOIN TravelOrderVehicle tov ON tov.vehicle.idVehicle = v.idVehicle " +
            "LEFT JOIN tov.travelOrder to " +
            "WHERE v.vehicleStatus = 'Active' " +
            "AND (to.status IS NULL OR to.status <> 'IN_PROGRESS')")
    List<Vehicle> findAvailableVehicles();

    /**
     * Find available vehicles filtered by location IDs
     */
    @Query("SELECT DISTINCT v FROM Vehicle v " +
            "LEFT JOIN v.vehicleLocation vl " +
            "LEFT JOIN TravelOrderVehicle tov ON tov.vehicle.idVehicle = v.idVehicle " +
            "LEFT JOIN tov.travelOrder to " +
            "WHERE vl.locationUnit.idLocationUnit IN :locationIds " +
            "AND v.vehicleStatus = 'Active' " +
            "AND (to.status IS NULL OR to.status <> 'IN_PROGRESS')")
    List<Vehicle> findAvailableVehiclesByLocations(@Param("locationIds") List<Long> locationIds);
}
