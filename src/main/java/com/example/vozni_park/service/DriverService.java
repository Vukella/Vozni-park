package com.example.vozni_park.service;

import com.example.vozni_park.dto.request.DriverRequestDTO;
import com.example.vozni_park.dto.response.DriverResponseDTO;
import com.example.vozni_park.entity.Driver;
import com.example.vozni_park.entity.LicenseRevocation;
import com.example.vozni_park.exception.BusinessRuleViolationException;
import com.example.vozni_park.exception.DuplicateEntityException;
import com.example.vozni_park.exception.EntityNotFoundException;
import com.example.vozni_park.exception.LocationAccessDeniedException;
import com.example.vozni_park.mapper.DriverMapper;
import com.example.vozni_park.repository.DriverRepository;
import com.example.vozni_park.repository.LicenseRevocationRepository;
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
public class DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;
    private final LocationFilterService locationFilterService;
    private final StatusCalculationService statusCalculationService;
    private final LicenseRevocationRepository licenseRevocationRepository;

    public List<DriverResponseDTO> getAllDrivers() {
        List<Driver> drivers;
        if (locationFilterService.isSuperAdmin()) {
            drivers = driverRepository.findAll();
        } else {
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            drivers = driverRepository.findByLocationIds(locationIds);
        }
        return driverMapper.toResponseDTOList(drivers);
    }

    public Optional<DriverResponseDTO> getDriverById(Long id) {
        Optional<Driver> driver = driverRepository.findById(id);
        if (driver.isPresent() && !locationFilterService.isSuperAdmin()) {
            Driver d = driver.get();
            if (d.getDriverLocation() != null) {
                Long locationId = d.getDriverLocation().getLocationUnit().getIdLocationUnit();
                if (!locationFilterService.hasAccessToLocation(locationId)) {
                    return Optional.empty();
                }
            }
        }
        return driver.map(driverMapper::toResponseDTO);
    }

    public Optional<DriverResponseDTO> getDriverBySapNumber(Long sapNumber) {
        Optional<Driver> driver = driverRepository.findBySapNumber(sapNumber);
        if (driver.isPresent() && !locationFilterService.isSuperAdmin()) {
            Driver d = driver.get();
            if (d.getDriverLocation() != null) {
                Long locationId = d.getDriverLocation().getLocationUnit().getIdLocationUnit();
                if (!locationFilterService.hasAccessToLocation(locationId)) {
                    return Optional.empty();
                }
            }
        }
        return driver.map(driverMapper::toResponseDTO);
    }

    public List<DriverResponseDTO> getDriversByStatus(String status) {
        List<Driver> drivers;
        if (locationFilterService.isSuperAdmin()) {
            drivers = driverRepository.findByStatus(status);
        } else {
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            drivers = driverRepository.findByLocationIdsAndStatus(locationIds, status);
        }
        return driverMapper.toResponseDTOList(drivers);
    }

    public List<DriverResponseDTO> searchDriversByName(String name) {
        List<Driver> drivers;
        if (locationFilterService.isSuperAdmin()) {
            drivers = driverRepository.findByFullNameContainingIgnoreCase(name);
        } else {
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            drivers = driverRepository.findByLocationIdsAndNameContaining(locationIds, name);
        }
        return driverMapper.toResponseDTOList(drivers);
    }

    public List<DriverResponseDTO> getDriversByLocation(Long locationId) {
        if (!locationFilterService.isSuperAdmin()) {
            locationFilterService.validateLocationAccess(locationId);
        }
        return driverMapper.toResponseDTOList(driverRepository.findByLocationId(locationId));
    }

    public List<DriverResponseDTO> getDriversWithoutLocation() {
        if (!locationFilterService.isSuperAdmin()) {
            throw new LocationAccessDeniedException("Only SUPER_ADMIN can view drivers without location assignment");
        }
        return driverMapper.toResponseDTOList(driverRepository.findDriversWithoutLocation());
    }

    public List<DriverResponseDTO> getAvailableDrivers() {
        List<Driver> drivers;
        if (locationFilterService.isSuperAdmin()) {
            drivers = driverRepository.findAvailableDrivers();
        } else {
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            drivers = driverRepository.findAvailableDriversByLocationIds(locationIds);
        }
        return driverMapper.toResponseDTOList(drivers);
    }

    @Transactional
    public DriverResponseDTO createDriver(DriverRequestDTO driverDTO) {
        if (driverRepository.existsBySapNumber(driverDTO.getSapNumber())) {
            throw new DuplicateEntityException("Driver", "SAP number", driverDTO.getSapNumber());
        }

        Driver driver = driverMapper.toEntity(driverDTO);
        driver.setLicenseRevoked(0);
        Driver saved = driverRepository.save(driver);

        statusCalculationService.recalculateAndSaveDriverStatus(saved.getIdDriver());

        return driverMapper.toResponseDTO(driverRepository.findById(saved.getIdDriver()).orElseThrow());
    }

    @Transactional
    public DriverResponseDTO updateDriver(Long id, DriverRequestDTO driverDTO) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver", id));

        if (!locationFilterService.isSuperAdmin() && driver.getDriverLocation() != null) {
            Long locationId = driver.getDriverLocation().getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(locationId);
        }

        if (!driverDTO.getSapNumber().equals(driver.getSapNumber()) &&
                driverRepository.existsBySapNumber(driverDTO.getSapNumber())) {
            throw new DuplicateEntityException("Driver", "SAP number", driverDTO.getSapNumber());
        }

        driverMapper.updateEntity(driver, driverDTO);
        driverRepository.save(driver);

        // Document changes may have shifted status
        statusCalculationService.recalculateAndSaveDriverStatus(id);

        return driverMapper.toResponseDTO(driverRepository.findById(id).orElseThrow());
    }

    /**
     * Records a license revocation for a driver.
     * Sets the fast flag on the driver row AND inserts an audit record
     * in license_revocation — both in the same transaction.
     * Immediately recalculates driver status to SUSPENDED.
     */
    @Transactional
    public DriverResponseDTO revokeDriverLicense(Long driverId, String reason, Long revokedByUserId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver", driverId));

        if (!locationFilterService.isSuperAdmin() && driver.getDriverLocation() != null) {
            Long locationId = driver.getDriverLocation().getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(locationId);
        }

        if (Integer.valueOf(1).equals(driver.getLicenseRevoked())) {
            throw new BusinessRuleViolationException("LICENSE_ALREADY_REVOKED",
                    "Driver '" + driver.getFullName() + "' already has a revoked license");
        }

        // Set fast flag
        driver.setLicenseRevoked(1);
        driverRepository.save(driver);

        // Insert audit record
        LicenseRevocation revocation = new LicenseRevocation();
        revocation.setDriver(driver);
        revocation.setRevocationDate(LocalDate.now());
        revocation.setRevocationReason(reason);
        revocation.setRevokedByUserId(revokedByUserId);
        licenseRevocationRepository.save(revocation);

        // Recalculate — will land on SUSPENDED
        statusCalculationService.recalculateAndSaveDriverStatus(driverId);

        log.info("License revoked for driver {} by user {}", driverId, revokedByUserId);
        return driverMapper.toResponseDTO(driverRepository.findById(driverId).orElseThrow());
    }

    /**
     * Restores a previously revoked license.
     * Clears the fast flag AND sets restoration_date on the audit record
     * — both in the same transaction.
     * Immediately recalculates driver status.
     */
    @Transactional
    public DriverResponseDTO restoreDriverLicense(Long driverId, Long restoredByUserId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver", driverId));

        if (!locationFilterService.isSuperAdmin() && driver.getDriverLocation() != null) {
            Long locationId = driver.getDriverLocation().getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(locationId);
        }

        if (!Integer.valueOf(1).equals(driver.getLicenseRevoked())) {
            throw new BusinessRuleViolationException("LICENSE_NOT_REVOKED",
                    "Driver '" + driver.getFullName() + "' does not have a revoked license");
        }

        LicenseRevocation activeRevocation = licenseRevocationRepository
                .findActiveRevocation(driverId)
                .orElseThrow(() -> new BusinessRuleViolationException("NO_ACTIVE_REVOCATION",
                        "No active revocation record found for driver: " + driverId));

        // Clear fast flag
        driver.setLicenseRevoked(0);
        driverRepository.save(driver);

        // Close audit record
        activeRevocation.setRestorationDate(LocalDate.now());
        activeRevocation.setRestoredByUserId(restoredByUserId);
        licenseRevocationRepository.save(activeRevocation);

        // Recalculate — will land on AVAILABLE or ON_DUTY depending on active orders
        statusCalculationService.recalculateAndSaveDriverStatus(driverId);

        log.info("License restored for driver {} by user {}", driverId, restoredByUserId);
        return driverMapper.toResponseDTO(driverRepository.findById(driverId).orElseThrow());
    }

    @Transactional
    public void deleteDriver(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver", id));

        if (!locationFilterService.isSuperAdmin() && driver.getDriverLocation() != null) {
            Long locationId = driver.getDriverLocation().getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(locationId);
        }

        driverRepository.deleteById(id);
    }

    public boolean driverExists(Long id) {
        return driverRepository.existsById(id);
    }

    public boolean isDriverAvailable(Long driverId) {
        return driverRepository.findById(driverId)
                .map(d -> d.getStatusCode() != null &&
                        d.getStatusCode() == com.example.vozni_park.enums.DriverStatus.AVAILABLE.getCode())
                .orElse(false);
    }
}
