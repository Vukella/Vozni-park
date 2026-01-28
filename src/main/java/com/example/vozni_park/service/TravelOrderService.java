package com.example.vozni_park.service;

import com.example.vozni_park.dto.request.TravelOrderRequestDTO;
import com.example.vozni_park.dto.response.TravelOrderResponseDTO;
import com.example.vozni_park.entity.TravelOrder;
import com.example.vozni_park.entity.TravelOrderCounter;
import com.example.vozni_park.entity.embeddable.TravelOrderCounterId;
import com.example.vozni_park.mapper.TravelOrderMapper;
import com.example.vozni_park.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelOrderService {

    private final TravelOrderRepository travelOrderRepository;
    private final TravelOrderMapper travelOrderMapper;
    private final TravelOrderCounterRepository travelOrderCounterRepository;
    private final LocationUnitRepository locationUnitRepository;
    private final AppUserRepository appUserRepository;

    /**
     * Get all travel orders - returns DTOs
     */
    public List<TravelOrderResponseDTO> getAllTravelOrders() {
        List<TravelOrder> travelOrders = travelOrderRepository.findAll();
        return travelOrderMapper.toResponseDTOList(travelOrders);
    }

    /**
     * Get travel order by ID
     */
    public Optional<TravelOrderResponseDTO> getTravelOrderById(Long id) {
        return travelOrderRepository.findById(id)
                .map(travelOrderMapper::toResponseDTO);
    }

    /**
     * Get travel order by work order number
     */
    public Optional<TravelOrderResponseDTO> getTravelOrderByWorkOrderNumber(String workOrderNumber) {
        return travelOrderRepository.findByWorkOrderNumber(workOrderNumber)
                .map(travelOrderMapper::toResponseDTO);
    }

    /**
     * Get travel orders by status
     */
    public List<TravelOrderResponseDTO> getTravelOrdersByStatus(String status) {
        List<TravelOrder> travelOrders = travelOrderRepository.findByStatus(status);
        return travelOrderMapper.toResponseDTOList(travelOrders);
    }

    /**
     * Get travel orders by location
     */
    public List<TravelOrderResponseDTO> getTravelOrdersByLocation(Long locationId) {
        List<TravelOrder> travelOrders = travelOrderRepository.findByLocationId(locationId);
        return travelOrderMapper.toResponseDTOList(travelOrders);
    }

    /**
     * Get active travel orders for a location
     */
    public List<TravelOrderResponseDTO> getActiveTravelOrdersByLocation(Long locationId) {
        List<TravelOrder> travelOrders = travelOrderRepository.findActiveByLocation(locationId);
        return travelOrderMapper.toResponseDTOList(travelOrders);
    }

    /**
     * Get travel orders by driver
     */
    public List<TravelOrderResponseDTO> getTravelOrdersByDriver(Long driverId) {
        List<TravelOrder> travelOrders = travelOrderRepository.findByDriverId(driverId);
        return travelOrderMapper.toResponseDTOList(travelOrders);
    }

    /**
     * Get travel orders by vehicle
     */
    public List<TravelOrderResponseDTO> getTravelOrdersByVehicle(Long vehicleId) {
        List<TravelOrder> travelOrders = travelOrderRepository.findByVehicleId(vehicleId);
        return travelOrderMapper.toResponseDTOList(travelOrders);
    }

    /**
     * Get travel orders by date range
     */
    public List<TravelOrderResponseDTO> getTravelOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        List<TravelOrder> travelOrders = travelOrderRepository.findByDateRange(startDate, endDate);
        return travelOrderMapper.toResponseDTOList(travelOrders);
    }

    /**
     * Create new travel order from DTO
     */
    @Transactional
    public TravelOrderResponseDTO createTravelOrder(TravelOrderRequestDTO travelOrderDTO) {
        // Validation: Check if location exists
        if (!locationUnitRepository.existsById(travelOrderDTO.getLocationId())) {
            throw new IllegalArgumentException("Location not found with id: " + travelOrderDTO.getLocationId());
        }

        // Validation: Check if created by user exists
        if (travelOrderDTO.getCreatedByUserId() != null &&
                !appUserRepository.existsById(travelOrderDTO.getCreatedByUserId())) {
            throw new IllegalArgumentException("User not found with id: " + travelOrderDTO.getCreatedByUserId());
        }

        // Validation: Check if work order number already exists
        if (travelOrderDTO.getWorkOrderNumber() != null &&
                travelOrderRepository.existsByWorkOrderNumber(travelOrderDTO.getWorkOrderNumber())) {
            throw new IllegalArgumentException("Work order number '" + travelOrderDTO.getWorkOrderNumber() + "' already exists");
        }

        // Validation: Date range check
        if (travelOrderDTO.getDateFrom() != null && travelOrderDTO.getDateTo() != null &&
                travelOrderDTO.getDateTo().isBefore(travelOrderDTO.getDateFrom())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        // Convert DTO to entity
        TravelOrder travelOrder = travelOrderMapper.toEntity(travelOrderDTO);

        // Auto-generate travel order number if not provided
        if (travelOrder.getTravelOrderNumber() == null) {
            String generatedNumber = generateTravelOrderNumber(travelOrderDTO.getLocationId());
            travelOrder.setTravelOrderNumber(generatedNumber);
        }

        // Set creation time
        if (travelOrder.getCreationTime() == null) {
            travelOrder.setCreationTime(LocalDateTime.now());
        }

        // Save and return DTO
        TravelOrder saved = travelOrderRepository.save(travelOrder);

        // TODO: Handle driver and vehicle assignments if provided in DTO

        return travelOrderMapper.toResponseDTO(saved);
    }

    /**
     * Generate unique travel order number for a location
     */
    private String generateTravelOrderNumber(Long locationId) {
        int currentYear = LocalDate.now().getYear();

        // Get or create counter with pessimistic lock (thread-safe)
        TravelOrderCounter counter = travelOrderCounterRepository
                .findByLocationAndYearForUpdate(locationId, currentYear)
                .orElseGet(() -> {
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

        // Format: LOC-YYYY-NNNN
        return String.format("%d-%d-%04d", locationId, currentYear, nextNumber);
    }

    /**
     * Update existing travel order from DTO
     */
    @Transactional
    public TravelOrderResponseDTO updateTravelOrder(Long id, TravelOrderRequestDTO travelOrderDTO) {
        TravelOrder travelOrder = travelOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Travel order not found with id: " + id));

        // Validation: Check if location exists
        if (!locationUnitRepository.existsById(travelOrderDTO.getLocationId())) {
            throw new IllegalArgumentException("Location not found with id: " + travelOrderDTO.getLocationId());
        }

        // Validation: Check if work order number conflicts
        if (travelOrderDTO.getWorkOrderNumber() != null &&
                !travelOrderDTO.getWorkOrderNumber().equals(travelOrder.getWorkOrderNumber()) &&
                travelOrderRepository.existsByWorkOrderNumber(travelOrderDTO.getWorkOrderNumber())) {
            throw new IllegalArgumentException("Work order number '" + travelOrderDTO.getWorkOrderNumber() + "' already exists");
        }

        // Validation: Date range check
        if (travelOrderDTO.getDateFrom() != null && travelOrderDTO.getDateTo() != null &&
                travelOrderDTO.getDateTo().isBefore(travelOrderDTO.getDateFrom())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        // Update entity from DTO
        travelOrderMapper.updateEntity(travelOrder, travelOrderDTO);

        // Save and return DTO
        TravelOrder updated = travelOrderRepository.save(travelOrder);
        return travelOrderMapper.toResponseDTO(updated);
    }

    /**
     * Update travel order status
     */
    @Transactional
    public TravelOrderResponseDTO updateTravelOrderStatus(Long id, String status) {
        TravelOrder travelOrder = travelOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Travel order not found with id: " + id));

        // Validation: Status change rules
        if ("COMPLETED".equals(status)) {
            if (travelOrder.getEndingMileage() == null) {
                throw new IllegalArgumentException("Cannot complete travel order without ending mileage");
            }
            if (travelOrder.getStartingMileage() != null &&
                    travelOrder.getEndingMileage() < travelOrder.getStartingMileage()) {
                throw new IllegalArgumentException("Ending mileage cannot be less than starting mileage");
            }
        }

        travelOrder.setStatus(status);

        TravelOrder updated = travelOrderRepository.save(travelOrder);
        return travelOrderMapper.toResponseDTO(updated);
    }

    /**
     * Complete travel order
     */
    @Transactional
    public TravelOrderResponseDTO completeTravelOrder(Long id, Long endingMileage) {
        TravelOrder travelOrder = travelOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Travel order not found with id: " + id));

        // Validation
        if (travelOrder.getStartingMileage() != null && endingMileage < travelOrder.getStartingMileage()) {
            throw new IllegalArgumentException("Ending mileage cannot be less than starting mileage");
        }

        travelOrder.setEndingMileage(endingMileage);
        travelOrder.setStatus("COMPLETED");

        TravelOrder updated = travelOrderRepository.save(travelOrder);
        return travelOrderMapper.toResponseDTO(updated);
    }

    /**
     * Delete travel order
     */
    @Transactional
    public void deleteTravelOrder(Long id) {
        if (!travelOrderRepository.existsById(id)) {
            throw new IllegalArgumentException("Travel order not found with id: " + id);
        }
        travelOrderRepository.deleteById(id);
    }

    /**
     * Check if travel order exists
     */
    public boolean travelOrderExists(Long id) {
        return travelOrderRepository.existsById(id);
    }
}