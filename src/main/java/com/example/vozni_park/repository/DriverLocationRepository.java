package com.example.vozni_park.repository;

import com.example.vozni_park.entity.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {
    
    // Custom query methods
    Optional<DriverLocation> findByDriverId(Long driverId);
    
    List<DriverLocation> findByLocationUnitId(Long locationUnitId);

    List<DriverLocation> findByLocationUnitIdIn(List<Long> locationIds);
    
    boolean existsByDriverId(Long driverId);
}
