package com.example.vozni_park.service;

import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.entity.Driver;
import com.example.vozni_park.entity.TrafficAccident;
import com.example.vozni_park.entity.Vehicle;
import com.example.vozni_park.exception.BusinessRuleViolationException;
import com.example.vozni_park.exception.EntityNotFoundException;
import com.example.vozni_park.repository.AppUserRepository;
import com.example.vozni_park.repository.DriverRepository;
import com.example.vozni_park.repository.TrafficAccidentRepository;
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
public class TrafficAccidentService {

    private final TrafficAccidentRepository trafficAccidentRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final AppUserRepository appUserRepository;
    private final LocationFilterService locationFilterService;

    public List<TrafficAccident> getAccidentsByVehicle(Long vehicleId) {
        vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", vehicleId));
        return trafficAccidentRepository.findByVehicleIdOrderByAccidentDateDesc(vehicleId);
    }

    public List<TrafficAccident> getAccidentsByDriver(Long driverId) {
        driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver", driverId));
        return trafficAccidentRepository.findByDriverIdOrderByAccidentDateDesc(driverId);
    }

    public TrafficAccident getAccidentById(Long accidentId) {
        return trafficAccidentRepository.findById(accidentId)
                .orElseThrow(() -> new EntityNotFoundException("TrafficAccident", accidentId));
    }

    /**
     * Records a new traffic accident.
     *
     * Captures snapshot fields at time of recording: driver license number,
     * license expiry date, and travel order number. These are stored as plain
     * strings/dates so the record remains accurate even if the driver's license
     * is later renewed or the travel order is closed.
     *
     * Traffic accidents do not affect vehicle or driver operational status —
     * they are documentary records only.
     */
    @Transactional
    public TrafficAccident reportAccident(Long vehicleId, TrafficAccident accident, String username) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", vehicleId));

        if (!locationFilterService.isSuperAdmin() && vehicle.getVehicleLocation() != null) {
            locationFilterService.validateLocationAccess(
                    vehicle.getVehicleLocation().getLocationUnit().getIdLocationUnit());
        }

        AppUser creator = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("AppUser", username));

        // Resolve driver if provided
        if (accident.getDriver() != null && accident.getDriver().getIdDriver() != null) {
            Driver driver = driverRepository.findById(accident.getDriver().getIdDriver())
                    .orElseThrow(() -> new EntityNotFoundException("Driver",
                            accident.getDriver().getIdDriver()));
            accident.setDriver(driver);
        }

        // Resolve statement driver if different from the main driver
        if (accident.getStatementGivenByDriver() != null
                && accident.getStatementGivenByDriver().getIdDriver() != null) {
            Driver statementDriver = driverRepository
                    .findById(accident.getStatementGivenByDriver().getIdDriver())
                    .orElseThrow(() -> new EntityNotFoundException("Driver",
                            accident.getStatementGivenByDriver().getIdDriver()));
            accident.setStatementGivenByDriver(statementDriver);
        }

        accident.setVehicle(vehicle);
        accident.setCreatedByUser(creator);

        if (accident.getAccidentDate() == null) {
            accident.setAccidentDate(LocalDate.now());
        }
        if (accident.getReportedToPolice() == null) {
            accident.setReportedToPolice(0);
        }

        // Validate: if not reported to police, reason must be provided
        if (Integer.valueOf(0).equals(accident.getReportedToPolice())
                && (accident.getNotReportedReason() == null
                    || accident.getNotReportedReason().isBlank())) {
            throw new BusinessRuleViolationException("MISSING_NOT_REPORTED_REASON",
                    "A reason must be provided when the accident was not reported to police");
        }

        TrafficAccident saved = trafficAccidentRepository.save(accident);
        log.info("Traffic accident recorded for vehicle {} by user {}", vehicleId, username);
        return saved;
    }

    /**
     * Updates accident details. Once recorded, only non-snapshot fields
     * (descriptions, damage value, other participants) may be edited.
     * Snapshot fields are immutable after creation.
     */
    @Transactional
    public TrafficAccident updateAccident(Long accidentId, TrafficAccident updatedData) {
        TrafficAccident accident = trafficAccidentRepository.findById(accidentId)
                .orElseThrow(() -> new EntityNotFoundException("TrafficAccident", accidentId));

        accident.setAccidentDescription(updatedData.getAccidentDescription());
        accident.setDamageDescription(updatedData.getDamageDescription());
        accident.setEstimatedDamageValue(updatedData.getEstimatedDamageValue());
        accident.setOtherParticipants(updatedData.getOtherParticipants());
        accident.setReportedToPolice(updatedData.getReportedToPolice());
        accident.setNotReportedReason(updatedData.getNotReportedReason());

        return trafficAccidentRepository.save(accident);
    }

    @Transactional
    public void deleteAccident(Long accidentId) {
        if (!trafficAccidentRepository.existsById(accidentId)) {
            throw new EntityNotFoundException("TrafficAccident", accidentId);
        }
        trafficAccidentRepository.deleteById(accidentId);
    }
}
