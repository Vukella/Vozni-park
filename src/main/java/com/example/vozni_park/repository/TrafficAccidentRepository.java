package com.example.vozni_park.repository;

import com.example.vozni_park.entity.TrafficAccident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrafficAccidentRepository extends JpaRepository<TrafficAccident, Long> {

    List<TrafficAccident> findByVehicleIdOrderByAccidentDateDesc(Long vehicleId);

    List<TrafficAccident> findByDriverIdOrderByAccidentDateDesc(Long driverId);
}
