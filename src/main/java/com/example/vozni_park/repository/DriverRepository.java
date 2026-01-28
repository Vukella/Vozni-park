package com.example.vozni_park.repository;

import com.example.vozni_park.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    @Query("SELECT d FROM Driver d WHERE d.status = 'ACTIVE' AND d.idDriver NOT IN " +
           "(SELECT dto.driver.idDriver FROM DriverTravelOrder dto WHERE dto.travelOrder.status = 'IN_PROGRESS')")
    List<Driver> findAvailableDrivers();
    
    boolean existsBySapNumber(Long sapNumber);
}
