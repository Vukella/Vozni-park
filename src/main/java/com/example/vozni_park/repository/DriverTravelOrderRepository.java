package com.example.vozni_park.repository;

import com.example.vozni_park.entity.DriverTravelOrder;
import com.example.vozni_park.entity.embeddable.DriverTravelOrderId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverTravelOrderRepository extends JpaRepository<DriverTravelOrder, DriverTravelOrderId> {
    
    // Custom query methods
    List<DriverTravelOrder> findByIdDriverId(Long driverId);
    
    List<DriverTravelOrder> findByIdTravelOrderId(Long travelOrderId);
    
    // Find active assignments for a driver
    @Query("SELECT dto FROM DriverTravelOrder dto WHERE dto.driver.idDriver = :driverId AND dto.travelOrder.status = 'IN_PROGRESS'")
    List<DriverTravelOrder> findActiveByDriverId(Long driverId);
    
    boolean existsByIdDriverIdAndIdTravelOrderId(Long driverId, Long travelOrderId);
}
