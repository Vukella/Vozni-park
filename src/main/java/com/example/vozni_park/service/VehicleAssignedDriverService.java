package com.example.vozni_park.service;

import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.entity.Driver;
import com.example.vozni_park.entity.Vehicle;
import com.example.vozni_park.entity.VehicleAssignedDriver;
import com.example.vozni_park.entity.VehicleAssignedDriverLog;
import com.example.vozni_park.exception.BusinessRuleViolationException;
import com.example.vozni_park.exception.EntityNotFoundException;
import com.example.vozni_park.repository.AppUserRepository;
import com.example.vozni_park.repository.DriverRepository;
import com.example.vozni_park.repository.VehicleAssignedDriverLogRepository;
import com.example.vozni_park.repository.VehicleAssignedDriverRepository;
import com.example.vozni_park.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class VehicleAssignedDriverService {

    private final VehicleAssignedDriverRepository assignedDriverRepository;
    private final VehicleAssignedDriverLogRepository assignedDriverLogRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final AppUserRepository appUserRepository;
    private final LocationFilterService locationFilterService;

    /**
     * Returns the current accountability assignment for a vehicle, if any.
     */
    public Optional<VehicleAssignedDriver> getCurrentAssignment(Long vehicleId) {
        vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", vehicleId));
        return assignedDriverRepository.findByVehicleId(vehicleId);
    }

    /**
     * Returns the full assignment history for a vehicle, most recent first.
     */
    public List<VehicleAssignedDriverLog> getAssignmentHistory(Long vehicleId) {
        vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", vehicleId));
        return assignedDriverLogRepository.findByVehicleIdOrderByChangeDateDesc(vehicleId);
    }

    /**
     * Assigns a driver as the accountable person for a vehicle.
     *
     * If the vehicle already has an assigned driver, the current assignment
     * is replaced. Both the old and new drivers are recorded in the log.
     * This preserves the full history of who was responsible for the vehicle
     * at any point in time.
     *
     * The log record is inserted in the same transaction as the assignment update,
     * so both are atomic — either both succeed or neither does.
     */
    @Transactional
    public VehicleAssignedDriver assignDriver(Long vehicleId, Long driverId,
                                              String note, String username) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", vehicleId));

        if (Integer.valueOf(1).equals(vehicle.getDecommissioned())) {
            throw new BusinessRuleViolationException("VEHICLE_DECOMMISSIONED",
                    "Cannot assign a driver to a decommissioned vehicle");
        }

        if (!locationFilterService.isSuperAdmin() && vehicle.getVehicleLocation() != null) {
            locationFilterService.validateLocationAccess(
                    vehicle.getVehicleLocation().getLocationUnit().getIdLocationUnit());
        }

        Driver newDriver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver", driverId));

        AppUser changedByUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("AppUser", username));

        // Capture current assignment before overwriting
        Driver previousDriver = assignedDriverRepository.findByVehicleId(vehicleId)
                .map(VehicleAssignedDriver::getDriver)
                .orElse(null);

        if (previousDriver != null && previousDriver.getIdDriver().equals(driverId)) {
            throw new BusinessRuleViolationException("SAME_DRIVER_ALREADY_ASSIGNED",
                    "Driver '" + newDriver.getFullName() + "' is already assigned to this vehicle");
        }

        // Insert log record BEFORE the assignment changes
        VehicleAssignedDriverLog logEntry = new VehicleAssignedDriverLog();
        logEntry.setVehicle(vehicle);
        logEntry.setPreviousDriver(previousDriver);
        logEntry.setNewDriver(newDriver);
        logEntry.setChangeDate(LocalDate.now());
        logEntry.setChangedByUser(changedByUser);
        assignedDriverLogRepository.save(logEntry);

        // Update or create the current assignment record
        VehicleAssignedDriver assignment = assignedDriverRepository
                .findByVehicleId(vehicleId)
                .orElseGet(VehicleAssignedDriver::new);

        assignment.setVehicle(vehicle);
        assignment.setDriver(newDriver);
        assignment.setAssignmentDate(LocalDate.now());
        assignment.setNote(note);
        VehicleAssignedDriver saved = assignedDriverRepository.save(assignment);

        log.info("Vehicle {} accountability assigned to driver {} by {}",
                vehicleId, driverId, username);
        return saved;
    }

    /**
     * Removes the current accountability assignment from a vehicle.
     * A log record is inserted to record the removal.
     */
    @Transactional
    public void removeAssignment(Long vehicleId, String username) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", vehicleId));

        VehicleAssignedDriver assignment = assignedDriverRepository.findByVehicleId(vehicleId)
                .orElseThrow(() -> new BusinessRuleViolationException("NO_ASSIGNMENT",
                        "Vehicle " + vehicleId + " has no current driver assignment"));

        AppUser changedByUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("AppUser", username));

        // Log the removal
        VehicleAssignedDriverLog logEntry = new VehicleAssignedDriverLog();
        logEntry.setVehicle(vehicle);
        logEntry.setPreviousDriver(assignment.getDriver());
        logEntry.setNewDriver(null);
        logEntry.setChangeDate(LocalDate.now());
        logEntry.setChangedByUser(changedByUser);
        assignedDriverLogRepository.save(logEntry);

        assignedDriverRepository.delete(assignment);
        log.info("Vehicle {} accountability assignment removed by {}", vehicleId, username);
    }
}
