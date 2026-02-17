package com.example.vozni_park.repository;

import com.example.vozni_park.entity.VehicleLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleLocationRepository extends JpaRepository<VehicleLocation, Long> {
    
    // Custom query methods
    Optional<VehicleLocation> findByVehicleId(Long vehicleId);
    
    List<VehicleLocation> findByLocationUnitId(Long locationUnitId);

    List<VehicleLocation> findByLocationUnitIdIn(List<Long> locationIds);
    
    boolean existsByVehicleId(Long vehicleId);
}
