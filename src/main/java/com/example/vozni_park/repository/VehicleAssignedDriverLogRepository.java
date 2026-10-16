package com.example.vozni_park.repository;

import com.example.vozni_park.entity.VehicleAssignedDriverLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleAssignedDriverLogRepository extends JpaRepository<VehicleAssignedDriverLog, Long> {

    List<VehicleAssignedDriverLog> findByVehicleIdOrderByChangeDateDesc(Long vehicleId);
}
