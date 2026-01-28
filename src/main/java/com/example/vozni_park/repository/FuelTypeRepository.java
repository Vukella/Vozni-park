package com.example.vozni_park.repository;

import com.example.vozni_park.entity.FuelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FuelTypeRepository extends JpaRepository<FuelType, Long> {
    
    // Custom query methods
    Optional<FuelType> findByName(String name);
    
    boolean existsByName(String name);
}
