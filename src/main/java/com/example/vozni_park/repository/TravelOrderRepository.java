package com.example.vozni_park.repository;

import com.example.vozni_park.entity.TravelOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TravelOrderRepository extends JpaRepository<TravelOrder, Long> {
    
    // Custom query methods
    Optional<TravelOrder> findByWorkOrderNumber(String workOrderNumber);
    
    Optional<TravelOrder> findByLocationIdAndTravelOrderNumber(Long locationId, String travelOrderNumber);
    
    List<TravelOrder> findByStatus(String status);
    
    List<TravelOrder> findByLocationId(Long locationId);
    
    List<TravelOrder> findByCreatedByUserId(Long userId);
    
    // Find travel orders by date range
    @Query("SELECT t FROM TravelOrder t WHERE t.dateFrom >= :startDate AND t.dateTo <= :endDate")
    List<TravelOrder> findByDateRange(LocalDate startDate, LocalDate endDate);
    
    // Find active travel orders for a location
    @Query("SELECT t FROM TravelOrder t WHERE t.locationId = :locationId AND t.status = 'IN_PROGRESS'")
    List<TravelOrder> findActiveByLocation(Long locationId);
    
    // Find travel orders by driver
    @Query("SELECT t FROM TravelOrder t JOIN t.driverTravelOrders dto WHERE dto.driver.idDriver = :driverId")
    List<TravelOrder> findByDriverId(Long driverId);
    
    // Find travel orders by vehicle
    @Query("SELECT t FROM TravelOrder t JOIN t.travelOrderVehicles tov WHERE tov.vehicle.idVehicle = :vehicleId")
    List<TravelOrder> findByVehicleId(Long vehicleId);
    
    boolean existsByWorkOrderNumber(String workOrderNumber);
}
