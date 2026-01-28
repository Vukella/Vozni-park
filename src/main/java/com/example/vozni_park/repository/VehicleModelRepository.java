package com.example.vozni_park.repository;

import com.example.vozni_park.entity.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {
    
    // Custom query methods
    List<VehicleModel> findByBrandId(Long brandId);
    
    Optional<VehicleModel> findByNameAndBrandId(String name, Long brandId);
    
    boolean existsByNameAndBrandId(String name, Long brandId);
}
