package com.example.vozni_park.service;

import com.example.vozni_park.dto.request.TravelOrderRequestDTO;
import com.example.vozni_park.dto.response.TravelOrderResponseDTO;
import com.example.vozni_park.entity.TravelOrder;
import com.example.vozni_park.entity.TravelOrderCounter;
import com.example.vozni_park.entity.embeddable.TravelOrderCounterId;
import com.example.vozni_park.mapper.TravelOrderMapper;
import com.example.vozni_park.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TravelOrderService {

    private final TravelOrderRepository travelOrderRepository;
    private final TravelOrderMapper travelOrderMapper;
    private final TravelOrderCounterRepository travelOrderCounterRepository;
    private final LocationUnitRepository locationUnitRepository;
    private final AppUserRepository appUserRepository;
    private final LocationFilterService locationFilterService;

    /**
     * Get all travel orders - automatically filtered by user's location(s)
     */
    public List<TravelOrderResponseDTO> getAllTravelOrders() {
        List<TravelOrder> travelOrders;

        if (locationFilterService.isSuperAdmin()) {
            // SUPER_ADMIN sees everything
            log.debug("SUPER_ADMIN access - fetching all travel orders");
            travelOrders = travelOrderRepository.findAll();
        } else {
            // LOCAL_ADMIN sees only their location(s)
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            log.debug("LOCAL_ADMIN access - fetching travel orders for locations: {}", locationIds);
            travelOrders = travelOrderRepository.findByLocationIds(locationIds);
        }

        return travelOrderMapper.toResponseDTOList(travelOrders);
    }

    /**
     * Get travel order by ID - validates location access
     */
    public Optional<TravelOrderResponseDTO> getTravelOrderById(Long id) {
        Optional<TravelOrder> travelOrder = travelOrderRepository.findById(id);

        // If not SUPER_ADMIN, validate location access
        if (travelOrder.isPresent() && !locationFilterService.isSuperAdmin()) {
            TravelOrder to = travelOrder.get();
            Long orderLocationId = to.getLocation().getIdLocationUnit();
            if (!locationFilterService.hasAccessToLocation(orderLocationId)) {
                log.warn("User {} denied access to travel order {} at location {}",
                        locationFilterService.getCurrentUsername(), id, orderLocationId);
                return Optional.empty();
            }
        }

        return travelOrder.map(travelOrderMapper::toResponseDTO);
    }

    /**
     * Get travel order by work order number - validates location access
     */
    public Optional<TravelOrderResponseDTO> getTravelOrderByWorkOrderNumber(String workOrderNumber) {
        Optional<TravelOrder> travelOrder = travelOrderRepository.findByWorkOrderNumber(workOrderNumber);

        // Validate location access
        if (travelOrder.isPresent() && !locationFilterService.isSuperAdmin()) {
            TravelOrder to = travelOrder.get();
            Long orderLocationId = to.getLocation().getIdLocationUnit();
            if (!locationFilterService.hasAccessToLocation(orderLocationId)) {
                return Optional.empty();
            }
        }

        return travelOrder.map(travelOrderMapper::toResponseDTO);
    }

    /**
     * Get travel orders by status - location filtered
     */
    public List<TravelOrderResponseDTO> getTravelOrdersByStatus(String status) {
        List<TravelOrder> travelOrders;

        if (locationFilterService.isSuperAdmin()) {
            travelOrders = travelOrderRepository.findByStatus(status);
        } else {
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            travelOrders = travelOrderRepository.findByLocationIdsAndStatus(locationIds, status);
        }

        return travelOrderMapper.toResponseDTOList(travelOrders);
    }

    /**
     * Get travel orders by location - validates location access
     */
    public List<TravelOrderResponseDTO> getTravelOrdersByLocation(Long locationId) {
        // Validate location access
        if (!locationFilterService.isSuperAdmin()) {
            locationFilterService.validateLocationAccess(locationId);
        }

        List<TravelOrder> travelOrders = travelOrderRepository.findByLocationId(locationId);
        return travelOrderMapper.toResponseDTOList(travelOrders);
    }

    /**
     * Get active travel orders for a location - validates location access
     */
    public List<TravelOrderResponseDTO> getActiveTravelOrdersByLocation(Long locationId) {
        // Validate location access
        if (!locationFilterService.isSuperAdmin()) {
            locationFilterService.validateLocationAccess(locationId);
        }

        List<TravelOrder> travelOrders = travelOrderRepository.findActiveByLocation(locationId);
        return travelOrderMapper.toResponseDTOList(travelOrders);
    }

    /**
     * Get travel orders by driver - location filtered (OPTIMIZED)
     */
    public List<TravelOrderResponseDTO> getTravelOrdersByDriver(Long driverId) {
        List<TravelOrder> travelOrders;

        if (locationFilterService.isSuperAdmin()) {
            travelOrders = travelOrderRepository.findByDriverId(driverId);
        } else {
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            travelOrders = travelOrderRepository.findByDriverIdAndLocationIds(driverId, locationIds);
        }

        return travelOrderMapper.toResponseDTOList(travelOrders);
    }

    /**
     * Get travel orders by vehicle - location filtered (OPTIMIZED)
     */
    public List<TravelOrderResponseDTO> getTravelOrdersByVehicle(Long vehicleId) {
        List<TravelOrder> travelOrders;

        if (locationFilterService.isSuperAdmin()) {
            travelOrders = travelOrderRepository.findByVehicleId(vehicleId);
        } else {
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            travelOrders = travelOrderRepository.findByVehicleIdAndLocationIds(vehicleId, locationIds);
        }

        return travelOrderMapper.toResponseDTOList(travelOrders);
    }

    /**
     * Get travel orders by date range - location filtered
     */
    public List<TravelOrderResponseDTO> getTravelOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        List<TravelOrder> travelOrders;

        if (locationFilterService.isSuperAdmin()) {
            travelOrders = travelOrderRepository.findByDateRange(startDate, endDate);
        } else {
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            travelOrders = travelOrderRepository.findByLocationIdsAndDateRange(locationIds, startDate, endDate);
        }

        return travelOrderMapper.toResponseDTOList(travelOrders);
    }

    /**
     * Create new travel order from DTO - validates location access
     */
    @Transactional
    public TravelOrderResponseDTO createTravelOrder(TravelOrderRequestDTO travelOrderDTO) {
        // Validation: Check if location exists
        if (!locationUnitRepository.existsById(travelOrderDTO.getLocationId())) {
            throw new IllegalArgumentException("Location not found with id: " + travelOrderDTO.getLocationId());
        }

        // Validation: LOCAL_ADMIN can only create orders for their assigned location(s)
        if (!locationFilterService.isSuperAdmin()) {
            locationFilterService.validateLocationAccess(travelOrderDTO.getLocationId());
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
     * Update existing travel order from DTO - validates location access
     */
    @Transactional
    public TravelOrderResponseDTO updateTravelOrder(Long id, TravelOrderRequestDTO travelOrderDTO) {
        TravelOrder travelOrder = travelOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Travel order not found with id: " + id));

        // Validate location access for LOCAL_ADMIN
        if (!locationFilterService.isSuperAdmin()) {
            Long orderLocationId = travelOrder.getLocation().getIdLocationUnit();
            locationFilterService.validateLocationAccess(orderLocationId);

            // Also validate new location if it's being changed
            if (!travelOrderDTO.getLocationId().equals(orderLocationId)) {
                locationFilterService.validateLocationAccess(travelOrderDTO.getLocationId());
            }
        }

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
     * Update travel order status - validates location access
     */
    @Transactional
    public TravelOrderResponseDTO updateTravelOrderStatus(Long id, String status) {
        TravelOrder travelOrder = travelOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Travel order not found with id: " + id));

        // Validate location access
        if (!locationFilterService.isSuperAdmin()) {
            Long orderLocationId = travelOrder.getLocation().getIdLocationUnit();
            locationFilterService.validateLocationAccess(orderLocationId);
        }

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
     * Complete travel order - validates location access
     */
    @Transactional
    public TravelOrderResponseDTO completeTravelOrder(Long id, Long endingMileage) {
        TravelOrder travelOrder = travelOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Travel order not found with id: " + id));

        // Validate location access
        if (!locationFilterService.isSuperAdmin()) {
            Long orderLocationId = travelOrder.getLocation().getIdLocationUnit();
            locationFilterService.validateLocationAccess(orderLocationId);
        }

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
     * Delete travel order - validates location access
     */
    @Transactional
    public void deleteTravelOrder(Long id) {
        TravelOrder travelOrder = travelOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Travel order not found with id: " + id));

        // Validate location access
        if (!locationFilterService.isSuperAdmin()) {
            Long orderLocationId = travelOrder.getLocation().getIdLocationUnit();
            locationFilterService.validateLocationAccess(orderLocationId);
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