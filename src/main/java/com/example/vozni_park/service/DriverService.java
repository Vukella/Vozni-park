package com.example.vozni_park.service;

import com.example.vozni_park.dto.request.DriverRequestDTO;
import com.example.vozni_park.dto.response.DriverResponseDTO;
import com.example.vozni_park.entity.Driver;
import com.example.vozni_park.mapper.DriverMapper;
import com.example.vozni_park.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Get all drivers - automatically filtered by user's location(s)
     */
    public List<DriverResponseDTO> getAllDrivers() {
        List<Driver> drivers;

        if (locationFilterService.isSuperAdmin()) {
            // SUPER_ADMIN sees everything
            log.debug("SUPER_ADMIN access - fetching all drivers");
            drivers = driverRepository.findAll();
        } else {
            // LOCAL_ADMIN sees only their location(s)
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            log.debug("LOCAL_ADMIN access - fetching drivers for locations: {}", locationIds);
            drivers = driverRepository.findByLocationIds(locationIds);
        }

        return driverMapper.toResponseDTOList(drivers);
    }

    /**
     * Get driver by ID - validates location access
     */
    public Optional<DriverResponseDTO> getDriverById(Long id) {
        Optional<Driver> driver = driverRepository.findById(id);

        // If not SUPER_ADMIN, validate location access
        if (driver.isPresent() && !locationFilterService.isSuperAdmin()) {
            Driver d = driver.get();
            if (d.getDriverLocation() != null) {
                Long driverLocationId = d.getDriverLocation().getLocationUnit().getIdLocationUnit();
                if (!locationFilterService.hasAccessToLocation(driverLocationId)) {
                    log.warn("User {} denied access to driver {} at location {}",
                            locationFilterService.getCurrentUsername(), id, driverLocationId);
                    return Optional.empty();
                }
            }
        }

        return driver.map(driverMapper::toResponseDTO);
    }

    /**
     * Get driver by SAP number - validates location access
     */
    public Optional<DriverResponseDTO> getDriverBySapNumber(Long sapNumber) {
        Optional<Driver> driver = driverRepository.findBySapNumber(sapNumber);

        // Validate location access
        if (driver.isPresent() && !locationFilterService.isSuperAdmin()) {
            Driver d = driver.get();
            if (d.getDriverLocation() != null) {
                Long driverLocationId = d.getDriverLocation().getLocationUnit().getIdLocationUnit();
                if (!locationFilterService.hasAccessToLocation(driverLocationId)) {
                    return Optional.empty();
                }
            }
        }

        return driver.map(driverMapper::toResponseDTO);
    }

    /**
     * Get drivers by status - location filtered
     */
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

    /**
     * Search drivers by name - location filtered
     */
    public List<DriverResponseDTO> searchDriversByName(String name) {
        List<Driver> drivers;

        if (locationFilterService.isSuperAdmin()) {
            drivers = driverRepository.findByFullNameContainingIgnoreCase(name);
        } else {
            // Use efficient query instead of stream filter
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            drivers = driverRepository.findByLocationIdsAndNameContaining(locationIds, name);
        }

        return driverMapper.toResponseDTOList(drivers);
    }

    /**
     * Get drivers by location - validates location access
     */
    public List<DriverResponseDTO> getDriversByLocation(Long locationId) {
        // Validate location access
        if (!locationFilterService.isSuperAdmin()) {
            locationFilterService.validateLocationAccess(locationId);
        }

        List<Driver> drivers = driverRepository.findByLocationId(locationId);
        return driverMapper.toResponseDTOList(drivers);
    }

    /**
     * Get drivers without location - SUPER_ADMIN only
     */
    public List<DriverResponseDTO> getDriversWithoutLocation() {
        // Only SUPER_ADMIN can see unassigned drivers
        if (!locationFilterService.isSuperAdmin()) {
            log.warn("LOCAL_ADMIN attempted to access unassigned drivers");
            throw new SecurityException("Only SUPER_ADMIN can view drivers without location assignment");
        }

        List<Driver> drivers = driverRepository.findDriversWithoutLocation();
        return driverMapper.toResponseDTOList(drivers);
    }

    /**
     * Get available drivers (active and not on travel order) - location filtered
     */
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

    /**
     * Create new driver from DTO
     */
    @Transactional
    public DriverResponseDTO createDriver(DriverRequestDTO driverDTO) {
        // Validation: Check if SAP number already exists
        if (driverRepository.existsBySapNumber(driverDTO.getSapNumber())) {
            throw new IllegalArgumentException("Driver with SAP number " + driverDTO.getSapNumber() + " already exists");
        }

        // Convert DTO to entity
        Driver driver = driverMapper.toEntity(driverDTO);

        // Save and return DTO
        Driver saved = driverRepository.save(driver);
        return driverMapper.toResponseDTO(saved);
    }

    /**
     * Update existing driver from DTO - validates location access
     */
    @Transactional
    public DriverResponseDTO updateDriver(Long id, DriverRequestDTO driverDTO) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with id: " + id));

        // Validate location access for LOCAL_ADMIN
        if (!locationFilterService.isSuperAdmin() && driver.getDriverLocation() != null) {
            Long driverLocationId = driver.getDriverLocation().getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(driverLocationId);
        }

        // Validation: Check if new SAP number conflicts
        if (!driverDTO.getSapNumber().equals(driver.getSapNumber()) &&
                driverRepository.existsBySapNumber(driverDTO.getSapNumber())) {
            throw new IllegalArgumentException("Driver with SAP number " + driverDTO.getSapNumber() + " already exists");
        }

        // Update entity from DTO
        driverMapper.updateEntity(driver, driverDTO);

        // Save and return DTO
        Driver updated = driverRepository.save(driver);
        return driverMapper.toResponseDTO(updated);
    }

    /**
     * Update driver status - validates location access
     */
    @Transactional
    public DriverResponseDTO updateDriverStatus(Long id, String status, Integer statusCode) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with id: " + id));

        // Validate location access
        if (!locationFilterService.isSuperAdmin() && driver.getDriverLocation() != null) {
            Long driverLocationId = driver.getDriverLocation().getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(driverLocationId);
        }

        driver.setStatus(status);
        if (statusCode != null) {
            driver.setStatusCode(statusCode);
        }

        Driver updated = driverRepository.save(driver);
        return driverMapper.toResponseDTO(updated);
    }

    /**
     * Delete driver - validates location access
     */
    @Transactional
    public void deleteDriver(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with id: " + id));

        // Validate location access
        if (!locationFilterService.isSuperAdmin() && driver.getDriverLocation() != null) {
            Long driverLocationId = driver.getDriverLocation().getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(driverLocationId);
        }

        driverRepository.deleteById(id);
    }

    /**
     * Check if driver exists
     */
    public boolean driverExists(Long id) {
        return driverRepository.existsById(id);
    }

    /**
     * Check if driver is available (active and not on travel order)
     */
    public boolean isDriverAvailable(Long driverId) {
        // Get available drivers based on user's access level
        List<Driver> availableDrivers;

        if (locationFilterService.isSuperAdmin()) {
            availableDrivers = driverRepository.findAvailableDrivers();
        } else {
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            availableDrivers = driverRepository.findAvailableDriversByLocationIds(locationIds);
        }

        return availableDrivers.stream()
                .anyMatch(driver -> driver.getIdDriver().equals(driverId));
    }
}