package com.example.vozni_park.service;

import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.entity.Driver;
import com.example.vozni_park.entity.Vehicle;
import com.example.vozni_park.entity.VehicleFault;
import com.example.vozni_park.enums.VehicleStatus;
import com.example.vozni_park.exception.BusinessRuleViolationException;
import com.example.vozni_park.exception.EntityNotFoundException;
import com.example.vozni_park.exception.InvalidStatusTransitionException;
import com.example.vozni_park.repository.AppUserRepository;
import com.example.vozni_park.repository.DriverRepository;
import com.example.vozni_park.repository.VehicleFaultRepository;
import com.example.vozni_park.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class VehicleFaultService {

    private final VehicleFaultRepository vehicleFaultRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final AppUserRepository appUserRepository;
    private final StatusCalculationService statusCalculationService;
    private final LocationFilterService locationFilterService;

    public List<VehicleFault> getFaultsByVehicle(Long vehicleId) {
        vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", vehicleId));
        return vehicleFaultRepository.findByVehicleIdOrderByReportDateDesc(vehicleId);
    }

    public VehicleFault getFaultById(Long faultId) {
        return vehicleFaultRepository.findById(faultId)
                .orElseThrow(() -> new EntityNotFoundException("VehicleFault", faultId));
    }

    /**
     * Records a new vehicle fault.
     *
     * If vehicleInoperable = true, the vehicle's status immediately transitions
     * to IN_FAULT (priority 1) via StatusCalculationService.
     * This is the Java equivalent of Oracle TRG_KVAR_AUD firing on INSERT.
     */
    @Transactional
    public VehicleFault reportFault(Long vehicleId, VehicleFault fault, String username) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", vehicleId));

        if (Integer.valueOf(1).equals(vehicle.getDecommissioned())) {
            throw new BusinessRuleViolationException("VEHICLE_DECOMMISSIONED",
                    "Cannot report a fault on a decommissioned vehicle");
        }

        if (!locationFilterService.isSuperAdmin() && vehicle.getVehicleLocation() != null) {
            locationFilterService.validateLocationAccess(
                    vehicle.getVehicleLocation().getLocationUnit().getIdLocationUnit());
        }

        // Validate referenced driver exists
        if (fault.getReportedByDriver() != null && fault.getReportedByDriver().getIdDriver() != null) {
            Driver reporter = driverRepository.findById(fault.getReportedByDriver().getIdDriver())
                    .orElseThrow(() -> new EntityNotFoundException("Driver",
                            fault.getReportedByDriver().getIdDriver()));
            fault.setReportedByDriver(reporter);
        }

        AppUser creator = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("AppUser", username));

        fault.setVehicle(vehicle);
        fault.setCreatedByUser(creator);
        fault.setReportDate(LocalDate.now());

        if (fault.getVehicleInoperable() == null) {
            fault.setVehicleInoperable(0);
        }

        VehicleFault saved = vehicleFaultRepository.save(fault);

        // If inoperable, trigger status recalculation immediately — vehicle → IN_FAULT
        if (Integer.valueOf(1).equals(saved.getVehicleInoperable())) {
            statusCalculationService.recalculateAndSaveVehicleStatus(vehicleId);
            log.info("Vehicle {} marked IN_FAULT due to inoperable fault {}", vehicleId, saved.getIdVehicleFault());
        }

        return saved;
    }

    /**
     * Resolves a fault by setting the resolution date.
     *
     * If this was an inoperable fault, vehicle status is recalculated immediately.
     * The vehicle may return to AVAILABLE, or stay in a lower-priority state
     * if other conditions still apply (e.g. expired documents).
     * This is the Java equivalent of TRG_KVAR_AUD firing on UPDATE when
     * DATUM_OTKLANJANJA changes from NULL to a date.
     */
    @Transactional
    public VehicleFault resolveFault(Long faultId, LocalDate resolutionDate, String username) {
        VehicleFault fault = vehicleFaultRepository.findById(faultId)
                .orElseThrow(() -> new EntityNotFoundException("VehicleFault", faultId));

        if (fault.getResolutionDate() != null) {
            throw new BusinessRuleViolationException("FAULT_ALREADY_RESOLVED",
                    "Fault " + faultId + " is already resolved");
        }

        if (!locationFilterService.isSuperAdmin() && fault.getVehicle() != null
                && fault.getVehicle().getVehicleLocation() != null) {
            locationFilterService.validateLocationAccess(
                    fault.getVehicle().getVehicleLocation().getLocationUnit().getIdLocationUnit());
        }

        fault.setResolutionDate(resolutionDate != null ? resolutionDate : LocalDate.now());
        vehicleFaultRepository.save(fault);

        // Recalculate vehicle status — may clear IN_FAULT if no other active faults remain
        if (Integer.valueOf(1).equals(fault.getVehicleInoperable())) {
            statusCalculationService.recalculateAndSaveVehicleStatus(fault.getVehicle().getIdVehicle());
            log.info("Fault {} resolved — vehicle {} status recalculated",
                    faultId, fault.getVehicle().getIdVehicle());
        }

        return fault;
    }

    /**
     * Updates fault details. Resolution date changes are routed through resolveFault().
     */
    @Transactional
    public VehicleFault updateFault(Long faultId, VehicleFault updatedData) {
        VehicleFault fault = vehicleFaultRepository.findById(faultId)
                .orElseThrow(() -> new EntityNotFoundException("VehicleFault", faultId));

        if (fault.getResolutionDate() != null) {
            throw new InvalidStatusTransitionException("VehicleFault",
                    "RESOLVED", "edit — resolved faults cannot be modified");
        }

        fault.setFaultDescription(updatedData.getFaultDescription());
        fault.setMileageAtFault(updatedData.getMileageAtFault());
        fault.setTowRequired(updatedData.getTowRequired());
        fault.setServiceType(updatedData.getServiceType());

        boolean inoperableChanged = !fault.getVehicleInoperable().equals(updatedData.getVehicleInoperable());
        fault.setVehicleInoperable(updatedData.getVehicleInoperable());
        vehicleFaultRepository.save(fault);

        if (inoperableChanged) {
            statusCalculationService.recalculateAndSaveVehicleStatus(fault.getVehicle().getIdVehicle());
        }

        return fault;
    }

    public List<VehicleFault> getActiveInoperableFaults(Long vehicleId) {
        return vehicleFaultRepository.findActiveInoperableFaults(vehicleId);
    }
}
