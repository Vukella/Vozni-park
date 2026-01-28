package com.example.vozni_park.service;

import com.example.vozni_park.entity.TravelOrder;
import com.example.vozni_park.entity.TravelOrderCounter;
import com.example.vozni_park.entity.embeddable.TravelOrderCounterId;
import com.example.vozni_park.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TravelOrderService {
    
    private final TravelOrderRepository travelOrderRepository;
    private final TravelOrderCounterRepository travelOrderCounterRepository;
    private final LocationUnitRepository locationUnitRepository;
    private final AppUserRepository appUserRepository;
    
    /**
     * Get all travel orders
     */
    @Transactional(readOnly = true)
    public List<TravelOrder> getAllTravelOrders() {
        return travelOrderRepository.findAll();
    }
    
    /**
     * Get travel order by ID
     */
    @Transactional(readOnly = true)
    public Optional<TravelOrder> getTravelOrderById(Long id) {
        return travelOrderRepository.findById(id);
    }
    
    /**
     * Get travel order by work order number
     */
    @Transactional(readOnly = true)
    public Optional<TravelOrder> getTravelOrderByWorkOrderNumber(String workOrderNumber) {
        return travelOrderRepository.findByWorkOrderNumber(workOrderNumber);
    }
    
    /**
     * Get travel orders by status
     */
    @Transactional(readOnly = true)
    public List<TravelOrder> getTravelOrdersByStatus(String status) {
        return travelOrderRepository.findByStatus(status);
    }
    
    /**
     * Get travel orders by location
     */
    @Transactional(readOnly = true)
    public List<TravelOrder> getTravelOrdersByLocation(Long locationId) {
        return travelOrderRepository.findByLocationId(locationId);
    }
    
    /**
     * Get active travel orders for a location
     */
    @Transactional(readOnly = true)
    public List<TravelOrder> getActiveTravelOrdersByLocation(Long locationId) {
        return travelOrderRepository.findActiveByLocation(locationId);
    }
    
    /**
     * Get travel orders by driver
     */
    @Transactional(readOnly = true)
    public List<TravelOrder> getTravelOrdersByDriver(Long driverId) {
        return travelOrderRepository.findByDriverId(driverId);
    }
    
    /**
     * Get travel orders by vehicle
     */
    @Transactional(readOnly = true)
    public List<TravelOrder> getTravelOrdersByVehicle(Long vehicleId) {
        return travelOrderRepository.findByVehicleId(vehicleId);
    }
    
    /**
     * Get travel orders by date range
     */
    @Transactional(readOnly = true)
    public List<TravelOrder> getTravelOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        return travelOrderRepository.findByDateRange(startDate, endDate);
    }
    
    /**
     * Create new travel order with auto-generated travel order number
     * Format: LOCATION_CODE-YYYY-NNNN (e.g., BG-2025-0001)
     */
    public TravelOrder createTravelOrder(TravelOrder travelOrder) {
        // Validation: Check if location exists
        if (!locationUnitRepository.existsById(travelOrder.getLocationId())) {
            throw new IllegalArgumentException("Location not found with id: " + travelOrder.getLocationId());
        }
        
        // Validation: Check if created by user exists
        if (travelOrder.getCreatedByUserId() != null && 
            !appUserRepository.existsById(travelOrder.getCreatedByUserId())) {
            throw new IllegalArgumentException("User not found with id: " + travelOrder.getCreatedByUserId());
        }
        
        // Validation: Check if work order number already exists
        if (travelOrder.getWorkOrderNumber() != null && 
            travelOrderRepository.existsByWorkOrderNumber(travelOrder.getWorkOrderNumber())) {
            throw new IllegalArgumentException("Work order number '" + travelOrder.getWorkOrderNumber() + "' already exists");
        }
        
        // Validation: Date range check
        if (travelOrder.getDateFrom() != null && travelOrder.getDateTo() != null &&
            travelOrder.getDateTo().isBefore(travelOrder.getDateFrom())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        
        // Auto-generate travel order number if not provided
        if (travelOrder.getTravelOrderNumber() == null) {
            String generatedNumber = generateTravelOrderNumber(travelOrder.getLocationId());
            travelOrder.setTravelOrderNumber(generatedNumber);
        }
        
        // Set default status if not provided
        if (travelOrder.getStatus() == null) {
            travelOrder.setStatus("IN_PROGRESS");
        }
        
        // Set creation time
        if (travelOrder.getCreationTime() == null) {
            travelOrder.setCreationTime(LocalDateTime.now());
        }
        
        return travelOrderRepository.save(travelOrder);
    }
    
    /**
     * Generate unique travel order number for a location
     * Uses pessimistic locking to prevent race conditions
     */
    private String generateTravelOrderNumber(Long locationId) {
        int currentYear = LocalDate.now().getYear();
        
        // Get or create counter with pessimistic lock (thread-safe)
        TravelOrderCounter counter = travelOrderCounterRepository
            .findByLocationAndYearForUpdate(locationId, currentYear)
            .orElseGet(() -> {
                // Create new counter for this location/year
                TravelOrderCounterId id = new TravelOrderCounterId(locationId, currentYear);
                TravelOrderCounter newCounter = new TravelOrderCounter();
                newCounter.setId(id);
                newCounter.setLastNumber(0);
                return newCounter;
            });
        
        // Increment counter
        int nextNumber = counter.getLastNumber() + 1;
        counter.setLastNumber(nextNumber);
        travelOrderCounterRepository.save(counter);
        
        // Format: LOC-YYYY-NNNN (e.g., 1-2025-0001)
        return String.format("%d-%d-%04d", locationId, currentYear, nextNumber);
    }
    
    /**
     * Update existing travel order
     */
    public TravelOrder updateTravelOrder(Long id, TravelOrder travelOrderDetails) {
        TravelOrder travelOrder = travelOrderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Travel order not found with id: " + id));
        
        // Validation: Check if location exists
        if (!locationUnitRepository.existsById(travelOrderDetails.getLocationId())) {
            throw new IllegalArgumentException("Location not found with id: " + travelOrderDetails.getLocationId());
        }
        
        // Validation: Check if work order number conflicts
        if (travelOrderDetails.getWorkOrderNumber() != null &&
            !travelOrderDetails.getWorkOrderNumber().equals(travelOrder.getWorkOrderNumber()) &&
            travelOrderRepository.existsByWorkOrderNumber(travelOrderDetails.getWorkOrderNumber())) {
            throw new IllegalArgumentException("Work order number '" + travelOrderDetails.getWorkOrderNumber() + "' already exists");
        }
        
        // Validation: Date range check
        if (travelOrderDetails.getDateFrom() != null && travelOrderDetails.getDateTo() != null &&
            travelOrderDetails.getDateTo().isBefore(travelOrderDetails.getDateFrom())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        
        // Update fields
        travelOrder.setDateFrom(travelOrderDetails.getDateFrom());
        travelOrder.setDateTo(travelOrderDetails.getDateTo());
        travelOrder.setWorkOrderNumber(travelOrderDetails.getWorkOrderNumber());
        travelOrder.setStartingMileage(travelOrderDetails.getStartingMileage());
        travelOrder.setEndingMileage(travelOrderDetails.getEndingMileage());
        travelOrder.setStatus(travelOrderDetails.getStatus());
        // Note: locationId, travelOrderNumber, and createdByUserId should not be changed after creation
        
        return travelOrderRepository.save(travelOrder);
    }
    
    /**
     * Update travel order status
     */
    public TravelOrder updateTravelOrderStatus(Long id, String status) {
        TravelOrder travelOrder = travelOrderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Travel order not found with id: " + id));
        
        // Validation: Status change rules
        if ("COMPLETED".equals(status)) {
            // Ensure ending mileage is set
            if (travelOrder.getEndingMileage() == null) {
                throw new IllegalArgumentException("Cannot complete travel order without ending mileage");
            }
            // Ensure ending mileage >= starting mileage
            if (travelOrder.getStartingMileage() != null && 
                travelOrder.getEndingMileage() < travelOrder.getStartingMileage()) {
                throw new IllegalArgumentException("Ending mileage cannot be less than starting mileage");
            }
        }
        
        travelOrder.setStatus(status);
        
        return travelOrderRepository.save(travelOrder);
    }
    
    /**
     * Complete travel order (set status to COMPLETED and set ending mileage)
     */
    public TravelOrder completeTravelOrder(Long id, Long endingMileage) {
        TravelOrder travelOrder = travelOrderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Travel order not found with id: " + id));
        
        // Validation
        if (travelOrder.getStartingMileage() != null && endingMileage < travelOrder.getStartingMileage()) {
            throw new IllegalArgumentException("Ending mileage cannot be less than starting mileage");
        }
        
        travelOrder.setEndingMileage(endingMileage);
        travelOrder.setStatus("COMPLETED");
        
        return travelOrderRepository.save(travelOrder);
    }
    
    /**
     * Delete travel order by ID
     */
    public void deleteTravelOrder(Long id) {
        if (!travelOrderRepository.existsById(id)) {
            throw new IllegalArgumentException("Travel order not found with id: " + id);
        }
        
        // TODO: Additional validation - check if travel order can be deleted
        // (e.g., only delete if status is DRAFT or CANCELLED)
        
        travelOrderRepository.deleteById(id);
    }
    
    /**
     * Check if travel order exists
     */
    @Transactional(readOnly = true)
    public boolean travelOrderExists(Long id) {
        return travelOrderRepository.existsById(id);
    }
}
