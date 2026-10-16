package com.example.vozni_park.repository;

import com.example.vozni_park.entity.VehicleAssignedDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleAssignedDriverRepository extends JpaRepository<VehicleAssignedDriver, Long> {

    Optional<VehicleAssignedDriver> findByVehicleId(Long vehicleId);

    boolean existsByVehicleId(Long vehicleId);
}
