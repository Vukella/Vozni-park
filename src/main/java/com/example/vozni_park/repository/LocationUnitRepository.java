package com.example.vozni_park.repository;

import com.example.vozni_park.entity.LocationUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationUnitRepository extends JpaRepository<LocationUnit, Long> {
    
    // Custom query methods
    Optional<LocationUnit> findByLocationName(String locationName);
    
    boolean existsByLocationName(String locationName);
}
