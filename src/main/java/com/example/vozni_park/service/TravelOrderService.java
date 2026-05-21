package com.example.vozni_park.service;

import com.example.vozni_park.dto.request.TravelOrderRequestDTO;
import com.example.vozni_park.dto.response.TravelOrderResponseDTO;
import com.example.vozni_park.entity.*;
import com.example.vozni_park.entity.embeddable.DriverTravelOrderId;
import com.example.vozni_park.entity.embeddable.TravelOrderCounterId;
import com.example.vozni_park.entity.embeddable.TravelOrderVehicleId;
import com.example.vozni_park.enums.DriverStatus;
import com.example.vozni_park.enums.TravelOrderStatus;
import com.example.vozni_park.enums.VehicleStatus;
import com.example.vozni_park.exception.BusinessRuleViolationException;
import com.example.vozni_park.exception.DuplicateEntityException;
import com.example.vozni_park.exception.EntityNotFoundException;
import com.example.vozni_park.exception.InvalidStatusTransitionException;
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
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverTravelOrderRepository driverTravelOrderRepository;
    private final TravelOrderVehicleRepository travelOrderVehicleRepository;
    private final StatusCalculationService statusCalculationService;

    public List<TravelOrderResponseDTO> getAllTravelOrders() {
        List<TravelOrder> travelOrders;
        if (locationFilterService.isSuperAdmin()) {
            travelOrders = travelOrderRepository.findAll();
        } else {
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            travelOrders = travelOrderRepository.findByLocationIds(locationIds);
        }
        return travelOrderMapper.toResponseDTOList(travelOrders);
    }

    public Optional<TravelOrderResponseDTO> getTravelOrderById(Long id) {
        Optional<TravelOrder> travelOrder = travelOrderRepository.findById(id);
        if (travelOrder.isPresent() && !locationFilterService.isSuperAdmin()) {
            Long orderLocationId = travelOrder.get().getLocation().getIdLocationUnit();
            if (!locationFilterService.hasAccessToLocation(orderLocationId)) {
                return Optional.empty();
            }
        }
        return travelOrder.map(travelOrderMapper::toResponseDTO);
    }

    public Optional<TravelOrderResponseDTO> getTravelOrderByWorkOrderNumber(String workOrderNumber) {
        Optional<TravelOrder> travelOrder = travelOrderRepository.findByWorkOrderNumber(workOrderNumber);
        if (travelOrder.isPresent() && !locationFilterService.isSuperAdmin()) {
            Long orderLocationId = travelOrder.get().getLocation().getIdLocationUnit();
            if (!locationFilterService.hasAccessToLocation(orderLocationId)) {
                return Optional.empty();
            }
        }
        return travelOrder.map(travelOrderMapper::toResponseDTO);
    }

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

    public List<TravelOrderResponseDTO> getTravelOrdersByLocation(Long locationId) {
        if (!locationFilterService.isSuperAdmin()) {
            locationFilterService.validateLocationAccess(locationId);
        }
        return travelOrderMapper.toResponseDTOList(
                travelOrderRepository.findByLocation_IdLocationUnit(locationId));
    }

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

    @Transactional
    public TravelOrderResponseDTO createTravelOrder(TravelOrderRequestDTO travelOrderDTO, String username) {
        AppUser creator = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("AppUser", username));

        if (!locationUnitRepository.existsById(travelOrderDTO.getLocationId())) {
            throw new EntityNotFoundException("LocationUnit", travelOrderDTO.getLocationId());
        }
        if (!locationFilterService.isSuperAdmin()) {
            locationFilterService.validateLocationAccess(travelOrderDTO.getLocationId());
        }
        if (travelOrderDTO.getWorkOrderNumber() != null &&
                travelOrderRepository.existsByWorkOrderNumber(travelOrderDTO.getWorkOrderNumber())) {
            throw new DuplicateEntityException("TravelOrder", "work order number",
                    travelOrderDTO.getWorkOrderNumber());
        }
        if (travelOrderDTO.getDateFrom() != null && travelOrderDTO.getDateTo() != null &&
                travelOrderDTO.getDateTo().isBefore(travelOrderDTO.getDateFrom())) {
            throw new BusinessRuleViolationException("INVALID_DATE_RANGE",
                    "End date cannot be before start date");
        }

        // Validate all drivers are AVAILABLE
        if (travelOrderDTO.getDriverIds() != null) {
            for (Long driverId : travelOrderDTO.getDriverIds()) {
                Driver driver = driverRepository.findById(driverId)
                        .orElseThrow(() -> new EntityNotFoundException("Driver", driverId));
                if (driver.getStatusCode() != null) {
                    DriverStatus currentStatus = DriverStatus.fromCode(driver.getStatusCode());
                    if (currentStatus.preventsAssignment()) {
                        throw new BusinessRuleViolationException("DRIVER_UNAVAILABLE",
                                "Driver '" + driver.getFullName() + "' has status " +
                                        currentStatus.getLabel() + " and cannot be assigned to a travel order");
                    }
                }
            }
        }

        // Validate all vehicles are AVAILABLE
        if (travelOrderDTO.getVehicleIds() != null) {
            for (Long vehicleId : travelOrderDTO.getVehicleIds()) {
                Vehicle vehicle = vehicleRepository.findById(vehicleId)
                        .orElseThrow(() -> new EntityNotFoundException("Vehicle", vehicleId));
                if (vehicle.getStatusCode() != null && vehicle.getStatusCode() != VehicleStatus.AVAILABLE.getCode()) {
                    throw new BusinessRuleViolationException("VEHICLE_UNAVAILABLE",
                            "Vehicle with SAP " + vehicle.getSapNumber() + " has status " +
                                    vehicle.getVehicleStatus() + " and cannot be assigned to a travel order");
                }
                if (Integer.valueOf(1).equals(vehicle.getDecommissioned())) {
                    throw new BusinessRuleViolationException("VEHICLE_DECOMMISSIONED",
                            "Vehicle with SAP " + vehicle.getSapNumber() + " is decommissioned");
                }
            }
        }

        TravelOrder travelOrder = travelOrderMapper.toEntity(travelOrderDTO);
        LocationUnit location = locationUnitRepository.findById(travelOrderDTO.getLocationId()).orElseThrow();
        travelOrder.setLocation(location);
        travelOrder.setCreatedByUser(creator);

        if (travelOrder.getTravelOrderNumber() == null) {
            travelOrder.setTravelOrderNumber(generateTravelOrderNumber(travelOrderDTO.getLocationId()));
        }
        if (travelOrder.getCreationTime() == null) {
            travelOrder.setCreationTime(LocalDateTime.now());
        }

        // Status set here — ACTIVE is the only valid initial status
        travelOrder.setStatus(TravelOrderStatus.ACTIVE.getLabel());
        travelOrder.setStatusCode(TravelOrderStatus.ACTIVE.getCode());

        TravelOrder saved = travelOrderRepository.save(travelOrder);

        // Save drivers
        if (travelOrderDTO.getDriverIds() != null && !travelOrderDTO.getDriverIds().isEmpty()) {
            List<Driver> drivers = driverRepository.findAllById(travelOrderDTO.getDriverIds());
            List<DriverTravelOrder> driverLinks = drivers.stream().map(d -> {
                var link = new DriverTravelOrder();
                link.setId(new DriverTravelOrderId());
                link.setTravelOrder(saved);
                link.setDriver(d);
                return link;
            }).toList();
            driverTravelOrderRepository.saveAll(driverLinks);
        }

        // Save vehicles
        if (travelOrderDTO.getVehicleIds() != null && !travelOrderDTO.getVehicleIds().isEmpty()) {
            List<Vehicle> vehicles = vehicleRepository.findAllById(travelOrderDTO.getVehicleIds());
            List<TravelOrderVehicle> vehicleLinks = vehicles.stream().map(v -> {
                var link = new TravelOrderVehicle();
                link.setId(new TravelOrderVehicleId());
                link.setTravelOrder(saved);
                link.setVehicle(v);
                return link;
            }).toList();
            travelOrderVehicleRepository.saveAll(vehicleLinks);
        }

        // Cascade status to all assigned vehicles and drivers
        TravelOrder full = travelOrderRepository.findById(saved.getIdTravelOrder()).orElseThrow();
        cascadeStatusAfterCreate(full);

        return travelOrderMapper.toResponseDTO(full);
    }

    /**
     * Close a travel order by entering the ending mileage.
     * Transitions: ACTIVE → COMPLETED, UNFINISHED → COMPLETED.
     */
    @Transactional
    public TravelOrderResponseDTO closeTravelOrder(Long id, Long endingMileage) {
        TravelOrder travelOrder = travelOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TravelOrder", id));

        if (!locationFilterService.isSuperAdmin()) {
            locationFilterService.validateLocationAccess(travelOrder.getLocation().getIdLocationUnit());
        }

        TravelOrderStatus currentStatus = TravelOrderStatus.fromCode(
                travelOrder.getStatusCode() != null ? travelOrder.getStatusCode() : 1);

        if (currentStatus.isTerminal()) {
            throw new InvalidStatusTransitionException("TravelOrder",
                    currentStatus.getLabel(), "close");
        }
        if (travelOrder.getStartingMileage() != null &&
                endingMileage < travelOrder.getStartingMileage()) {
            throw new BusinessRuleViolationException("INVALID_MILEAGE",
                    "Ending mileage cannot be less than starting mileage (" +
                            travelOrder.getStartingMileage() + ")");
        }

        travelOrder.setEndingMileage(endingMileage);
        travelOrderRepository.save(travelOrder);

        // StatusCalculationService owns the status transition and cascade
        statusCalculationService.completeTravelOrder(travelOrder);

        return travelOrderMapper.toResponseDTO(travelOrderRepository.findById(id).orElseThrow());
    }

    /**
     * Cancel a travel order. Only allowed from ACTIVE state.
     */
    @Transactional
    public TravelOrderResponseDTO cancelTravelOrder(Long id) {
        TravelOrder travelOrder = travelOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TravelOrder", id));

        if (!locationFilterService.isSuperAdmin()) {
            locationFilterService.validateLocationAccess(travelOrder.getLocation().getIdLocationUnit());
        }

        TravelOrderStatus currentStatus = TravelOrderStatus.fromCode(
                travelOrder.getStatusCode() != null ? travelOrder.getStatusCode() : 1);

        if (currentStatus != TravelOrderStatus.ACTIVE) {
            throw new InvalidStatusTransitionException("TravelOrder",
                    currentStatus.getLabel(),
                    "cancel — only ACTIVE orders can be cancelled");
        }

        // StatusCalculationService owns the status transition and cascade
        statusCalculationService.cancelTravelOrder(travelOrder);

        return travelOrderMapper.toResponseDTO(travelOrderRepository.findById(id).orElseThrow());
    }

    @Transactional
    public void deleteTravelOrder(Long id) {
        TravelOrder travelOrder = travelOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TravelOrder", id));

        if (!locationFilterService.isSuperAdmin()) {
            locationFilterService.validateLocationAccess(travelOrder.getLocation().getIdLocationUnit());
        }

        travelOrderRepository.deleteById(id);
    }

    public boolean travelOrderExists(Long id) {
        return travelOrderRepository.existsById(id);
    }

    // ── Private helpers ────────────────────────────────────────────────

    private String generateTravelOrderNumber(Long locationId) {
        int currentYear = LocalDate.now().getYear();
        TravelOrderCounter counter = travelOrderCounterRepository
                .findByLocationAndYearForUpdate(locationId, currentYear)
                .orElseGet(() -> {
                    TravelOrderCounter newCounter = new TravelOrderCounter();
                    newCounter.setId(new TravelOrderCounterId(locationId, currentYear));
                    newCounter.setLastNumber(0);
                    return newCounter;
                });
        int nextNumber = counter.getLastNumber() + 1;
        counter.setLastNumber(nextNumber);
        travelOrderCounterRepository.save(counter);
        return String.format("%d-%d-%04d", locationId, currentYear, nextNumber);
    }

    private void cascadeStatusAfterCreate(TravelOrder travelOrder) {
        if (travelOrder.getTravelOrderVehicles() != null) {
            travelOrder.getTravelOrderVehicles().forEach(tov ->
                    statusCalculationService.recalculateAndSaveVehicleStatus(
                            tov.getVehicle().getIdVehicle()));
        }
        if (travelOrder.getDriverTravelOrders() != null) {
            travelOrder.getDriverTravelOrders().forEach(dto ->
                    statusCalculationService.recalculateAndSaveDriverStatus(
                            dto.getDriver().getIdDriver()));
        }
    }
}