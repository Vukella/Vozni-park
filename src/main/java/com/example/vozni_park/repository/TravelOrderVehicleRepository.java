package com.example.vozni_park.repository;

import com.example.vozni_park.entity.TravelOrderVehicle;
import com.example.vozni_park.entity.embeddable.TravelOrderVehicleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelOrderVehicleRepository extends JpaRepository<TravelOrderVehicle, TravelOrderVehicleId> {
    
    // Custom query methods
    List<TravelOrderVehicle> findByIdVehicleId(Long vehicleId);
    
    List<TravelOrderVehicle> findByIdTravelOrderId(Long travelOrderId);
    
    // Find active assignments for a vehicle
    @Query("SELECT tov FROM TravelOrderVehicle tov WHERE tov.vehicle.idVehicle = :vehicleId AND tov.travelOrder.status = 'IN_PROGRESS'")
    List<TravelOrderVehicle> findActiveByVehicleId(Long vehicleId);
    
    boolean existsByIdVehicleIdAndIdTravelOrderId(Long vehicleId, Long travelOrderId);
}
