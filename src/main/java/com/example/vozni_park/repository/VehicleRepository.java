package com.example.vozni_park.repository;

import com.example.vozni_park.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    
    // Find vehicles by location
    @Query("SELECT v FROM Vehicle v JOIN v.vehicleLocation vl WHERE vl.locationUnit.idLocationUnit = :locationId")
    List<Vehicle> findByLocationId(Long locationId);
    
    // Find vehicles without location
    @Query("SELECT v FROM Vehicle v WHERE v.vehicleLocation IS NULL")
    List<Vehicle> findVehiclesWithoutLocation();
    
    boolean existsBySapNumber(Long sapNumber);
    
    boolean existsByChassisNumber(String chassisNumber);
}
