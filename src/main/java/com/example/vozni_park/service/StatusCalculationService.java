package com.example.vozni_park.service;

import com.example.vozni_park.entity.*;
import com.example.vozni_park.enums.DocumentStatus;
import com.example.vozni_park.enums.DriverStatus;
import com.example.vozni_park.enums.TravelOrderStatus;
import com.example.vozni_park.enums.VehicleStatus;
import com.example.vozni_park.exception.EntityNotFoundException;
import com.example.vozni_park.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Single source of truth for all entity status calculations.
 *
 * Design principles:
 * - Status is NEVER set directly by other services. All status writes go through
 *   recalculateAndSave*() methods in this service.
 * - Calculation methods (calculateVehicleStatus, calculateDriverStatus) are pure
 *   functions — they read state and return a result without writing to the DB.
 * - All expiry checks use a live LocalDate.now() comparison, never the stored
 *   status_code on the document row. This prevents stale data from hiding faults.
 *
 * Vehicle status priority (highest first):
 *   5 IN_FAULT        — active unresolved fault with vehicle_inoperable = 1
 *   4 INVALID         — any linked document expired (live date check)
 *   3 UNAVAILABLE     — has an UNFINISHED (overdue) travel order
 *   2 ON_DUTY         — has an ACTIVE travel order
 *   1 AVAILABLE       — none of the above (default)
 *
 * Driver status priority (highest first):
 *   4 SUSPENDED           — license_revoked flag = 1 (disciplinary/legal)
 *   3 INVALID_DOCUMENTS   — any document expired (live date check)
 *   2 ON_DUTY             — has an ACTIVE travel order
 *   1 AVAILABLE           — none of the above (default)
 *
 * Travel order status:
 *   0 CANCELLED  — manually cancelled from ACTIVE state only
 *   1 ACTIVE     — date_to in future, no ending mileage
 *   2 UNFINISHED — date_to passed, no ending mileage (set by daily refresh)
 *   3 COMPLETED  — ending mileage entered (terminal)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StatusCalculationService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final TravelOrderRepository travelOrderRepository;
    private final VehicleFaultRepository vehicleFaultRepository;

    // ══════════════════════════════════════════════════════════════════
    // VEHICLE STATUS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Calculates the correct status for a vehicle using live data.
     * Does not write to the database.
     */
    public VehicleStatus calculateVehicleStatus(Vehicle vehicle) {
        Long vehicleId = vehicle.getIdVehicle();

        // Priority 1 — IN_FAULT: active unresolved inoperable fault
        if (vehicleFaultRepository.hasActiveInoperableFault(vehicleId)) {
            log.debug("Vehicle {} → IN_FAULT (active inoperable fault)", vehicleId);
            return VehicleStatus.IN_FAULT;
        }

        // Priority 2 — INVALID: any linked document expired
        if (hasExpiredVehicleDocument(vehicle)) {
            log.debug("Vehicle {} → INVALID (expired document)", vehicleId);
            return VehicleStatus.INVALID;
        }

        // Priority 3 — UNAVAILABLE: has an UNFINISHED travel order
        if (travelOrderRepository.hasUnfinishedVehicleTravelOrder(vehicleId)) {
            log.debug("Vehicle {} → UNAVAILABLE (unfinished travel order)", vehicleId);
            return VehicleStatus.UNAVAILABLE;
        }

        // Priority 4 — ON_DUTY: has an ACTIVE travel order
        if (travelOrderRepository.hasActiveVehicleTravelOrder(vehicleId)) {
            log.debug("Vehicle {} → ON_DUTY (active travel order)", vehicleId);
            return VehicleStatus.ON_DUTY;
        }

        // Priority 5 — AVAILABLE: default
        log.debug("Vehicle {} → AVAILABLE", vehicleId);
        return VehicleStatus.AVAILABLE;
    }

    /**
     * Recalculates vehicle status and persists the result.
     * Called after any mutation that could affect vehicle status:
     * - travel order created/closed/cancelled
     * - fault recorded/resolved
     * - document updated (registration, casco, tachograph, fire extinguisher, first aid kit)
     */
    @Transactional
    public void recalculateAndSaveVehicleStatus(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", vehicleId));

        // Decommissioned vehicles do not participate in status calculation
        if (Integer.valueOf(1).equals(vehicle.getDecommissioned())) {
            log.debug("Vehicle {} is decommissioned — skipping status recalculation", vehicleId);
            return;
        }

        VehicleStatus newStatus = calculateVehicleStatus(vehicle);
        vehicle.setVehicleStatus(newStatus.getLabel());
        vehicle.setStatusCode(newStatus.getCode());
        vehicleRepository.save(vehicle);

        log.info("Vehicle {} status updated to {} (code {})", vehicleId, newStatus.getLabel(), newStatus.getCode());
    }

    /**
     * Recalculates status for multiple vehicles in one call.
     * Used when a travel order affects multiple vehicles simultaneously.
     */
    @Transactional
    public void recalculateAndSaveVehicleStatuses(List<Long> vehicleIds) {
        vehicleIds.forEach(this::recalculateAndSaveVehicleStatus);
    }

    // ══════════════════════════════════════════════════════════════════
    // DRIVER STATUS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Calculates the correct status for a driver using live data.
     * Does not write to the database.
     */
    public DriverStatus calculateDriverStatus(Driver driver) {
        Long driverId = driver.getIdDriver();

        // Priority 1 — SUSPENDED: license actively revoked
        if (Integer.valueOf(1).equals(driver.getLicenseRevoked())) {
            log.debug("Driver {} → SUSPENDED (license revoked)", driverId);
            return DriverStatus.SUSPENDED;
        }

        // Priority 2 — INVALID_DOCUMENTS: any document expired
        if (hasExpiredDriverDocument(driver)) {
            log.debug("Driver {} → INVALID_DOCUMENTS (expired document)", driverId);
            return DriverStatus.INVALID_DOCUMENTS;
        }

        // Priority 3 — ON_DUTY: has an active travel order
        if (travelOrderRepository.hasActiveDriverTravelOrder(driverId)) {
            log.debug("Driver {} → ON_DUTY (active travel order)", driverId);
            return DriverStatus.ON_DUTY;
        }

        // Priority 4 — AVAILABLE: default
        log.debug("Driver {} → AVAILABLE", driverId);
        return DriverStatus.AVAILABLE;
    }

    /**
     * Recalculates driver status and persists the result.
     * Called after any mutation that could affect driver status:
     * - travel order created/closed/cancelled
     * - license revocation recorded or restored
     * - any driver document updated
     */
    @Transactional
    public void recalculateAndSaveDriverStatus(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver", driverId));

        DriverStatus newStatus = calculateDriverStatus(driver);
        driver.setStatus(newStatus.getLabel());
        driver.setStatusCode(newStatus.getCode());
        driverRepository.save(driver);

        log.info("Driver {} status updated to {} (code {})", driverId, newStatus.getLabel(), newStatus.getCode());
    }

    /**
     * Recalculates status for multiple drivers in one call.
     */
    @Transactional
    public void recalculateAndSaveDriverStatuses(List<Long> driverIds) {
        driverIds.forEach(this::recalculateAndSaveDriverStatus);
    }

    // ══════════════════════════════════════════════════════════════════
    // TRAVEL ORDER STATUS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Calculates the correct status for a travel order.
     * Does not write to the database.
     */
    public TravelOrderStatus calculateTravelOrderStatus(TravelOrder travelOrder) {
        // CANCELLED and COMPLETED are terminal — never overridden
        if (travelOrder.getStatusCode() != null) {
            if (travelOrder.getStatusCode() == TravelOrderStatus.CANCELLED.getCode()) {
                return TravelOrderStatus.CANCELLED;
            }
            if (travelOrder.getStatusCode() == TravelOrderStatus.COMPLETED.getCode()) {
                return TravelOrderStatus.COMPLETED;
            }
        }

        // COMPLETED: ending mileage entered
        if (travelOrder.getEndingMileage() != null) {
            return TravelOrderStatus.COMPLETED;
        }

        // UNFINISHED: date_to has passed with no ending mileage
        if (travelOrder.getDateTo() != null && LocalDate.now().isAfter(travelOrder.getDateTo())) {
            return TravelOrderStatus.UNFINISHED;
        }

        // ACTIVE: default
        return TravelOrderStatus.ACTIVE;
    }

    /**
     * Sets travel order status to COMPLETED and triggers recalculation
     * of all linked vehicles and drivers.
     */
    @Transactional
    public void completeTravelOrder(TravelOrder travelOrder) {
        travelOrder.setStatus(TravelOrderStatus.COMPLETED.getLabel());
        travelOrder.setStatusCode(TravelOrderStatus.COMPLETED.getCode());
        travelOrderRepository.save(travelOrder);

        cascadeStatusRecalculation(travelOrder);
    }

    /**
     * Sets travel order status to CANCELLED and triggers recalculation
     * of all linked vehicles and drivers. Only allowed from ACTIVE state.
     */
    @Transactional
    public void cancelTravelOrder(TravelOrder travelOrder) {
        travelOrder.setStatus(TravelOrderStatus.CANCELLED.getLabel());
        travelOrder.setStatusCode(TravelOrderStatus.CANCELLED.getCode());
        travelOrderRepository.save(travelOrder);

        cascadeStatusRecalculation(travelOrder);
    }

    /**
     * Marks a travel order as UNFINISHED (used by the daily refresh job).
     * Also cascades to linked vehicles and drivers.
     */
    @Transactional
    public void markTravelOrderUnfinished(TravelOrder travelOrder) {
        travelOrder.setStatus(TravelOrderStatus.UNFINISHED.getLabel());
        travelOrder.setStatusCode(TravelOrderStatus.UNFINISHED.getCode());
        travelOrderRepository.save(travelOrder);

        cascadeStatusRecalculation(travelOrder);
    }

    // ══════════════════════════════════════════════════════════════════
    // DOCUMENT STATUS (shared by all expiry-tracked documents)
    // ══════════════════════════════════════════════════════════════════

    /**
     * Calculates document status from its validity dates.
     * Uses live LocalDate.now() — never relies on stored status_code.
     */
    public DocumentStatus calculateDocumentStatus(LocalDate dateTo) {
        if (dateTo != null && LocalDate.now().isAfter(dateTo)) {
            return DocumentStatus.EXPIRED;
        }
        return DocumentStatus.VALID;
    }

    // ══════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════

    private boolean hasExpiredVehicleDocument(Vehicle vehicle) {
        LocalDate today = LocalDate.now();

        // Registration (mandatory — always checked if linked)
        if (isExpired(vehicle.getRegistration() != null ? vehicle.getRegistration().getDateTo() : null, today)) {
            return true;
        }
        // Casco insurance (optional)
        if (isExpired(vehicle.getCascoInsurance() != null ? vehicle.getCascoInsurance().getDateTo() : null, today)) {
            return true;
        }
        // Tachograph device (optional)
        if (isExpired(vehicle.getTachographDevice() != null ? vehicle.getTachographDevice().getDateTo() : null, today)) {
            return true;
        }
        // Fire extinguisher (optional)
        if (isExpired(vehicle.getFireExtinguisher() != null ? vehicle.getFireExtinguisher().getDateTo() : null, today)) {
            return true;
        }
        // First aid kit (optional — uses expiry_date, not date_to)
        if (vehicle.getFirstAidKit() != null &&
                isExpired(vehicle.getFirstAidKit().getExpiryDate(), today)) {
            return true;
        }
        return false;
    }

    private boolean hasExpiredDriverDocument(Driver driver) {
        LocalDate today = LocalDate.now();

        // Medical certificate
        if (driver.getMedicalCertificate() != null &&
                isExpired(driver.getMedicalCertificate().getDateTo(), today)) {
            return true;
        }
        // Driver permit (licenca)
        if (driver.getDriverPermit() != null &&
                isExpired(driver.getDriverPermit().getDateTo(), today)) {
            return true;
        }
        // Driving licenses + their tachograph cards (all categories)
        if (driver.getDriverLicenseAssignments() != null) {
            for (DriverLicenseAssignment assignment : driver.getDriverLicenseAssignments()) {
                DriversLicense license = assignment.getDriversLicense();
                if (license == null) continue;
                // License expiry
                if (isExpired(license.getDateTo(), today)) {
                    return true;
                }
                // Tachograph card linked to this license
                if (license.getTachographCard() != null &&
                        isExpired(license.getTachographCard().getDateTo(), today)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * After a travel order changes state, recalculate status for all
     * linked vehicles and drivers. This is the Java equivalent of the
     * Oracle TRG_PUTNI_NALOG_AUD_PROPAGATE trigger.
     */
    private void cascadeStatusRecalculation(TravelOrder travelOrder) {
        if (travelOrder.getTravelOrderVehicles() != null) {
            travelOrder.getTravelOrderVehicles().forEach(tov ->
                    recalculateAndSaveVehicleStatus(tov.getVehicle().getIdVehicle())
            );
        }
        if (travelOrder.getDriverTravelOrders() != null) {
            travelOrder.getDriverTravelOrders().forEach(dto ->
                    recalculateAndSaveDriverStatus(dto.getDriver().getIdDriver())
            );
        }
    }

    private boolean isExpired(LocalDate dateTo, LocalDate today) {
        return dateTo != null && today.isAfter(dateTo);
    }
}
