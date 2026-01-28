package com.example.vozni_park.service;

import com.example.vozni_park.dto.request.DriverRequestDTO;
import com.example.vozni_park.dto.response.DriverResponseDTO;
import com.example.vozni_park.entity.Driver;
import com.example.vozni_park.mapper.DriverMapper;
import com.example.vozni_park.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

    /**
     * Get all drivers - returns DTOs
     */
    public List<DriverResponseDTO> getAllDrivers() {
        List<Driver> drivers = driverRepository.findAll();
        return driverMapper.toResponseDTOList(drivers);
    }

    /**
     * Get driver by ID
     */
    public Optional<DriverResponseDTO> getDriverById(Long id) {
        return driverRepository.findById(id)
                .map(driverMapper::toResponseDTO);
    }

    /**
     * Get driver by SAP number
     */
    public Optional<DriverResponseDTO> getDriverBySapNumber(Long sapNumber) {
        return driverRepository.findBySapNumber(sapNumber)
                .map(driverMapper::toResponseDTO);
    }

    /**
     * Get drivers by status
     */
    public List<DriverResponseDTO> getDriversByStatus(String status) {
        List<Driver> drivers = driverRepository.findByStatus(status);
        return driverMapper.toResponseDTOList(drivers);
    }

    /**
     * Search drivers by name
     */
    public List<DriverResponseDTO> searchDriversByName(String name) {
        List<Driver> drivers = driverRepository.findByFullNameContainingIgnoreCase(name);
        return driverMapper.toResponseDTOList(drivers);
    }

    /**
     * Get drivers by location
     */
    public List<DriverResponseDTO> getDriversByLocation(Long locationId) {
        List<Driver> drivers = driverRepository.findByLocationId(locationId);
        return driverMapper.toResponseDTOList(drivers);
    }

    /**
     * Get drivers without location
     */
    public List<DriverResponseDTO> getDriversWithoutLocation() {
        List<Driver> drivers = driverRepository.findDriversWithoutLocation();
        return driverMapper.toResponseDTOList(drivers);
    }

    /**
     * Get available drivers (active and not on travel order)
     */
    public List<DriverResponseDTO> getAvailableDrivers() {
        List<Driver> drivers = driverRepository.findAvailableDrivers();
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
     * Update existing driver from DTO
     */
    @Transactional
    public DriverResponseDTO updateDriver(Long id, DriverRequestDTO driverDTO) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with id: " + id));

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
     * Update driver status
     */
    @Transactional
    public DriverResponseDTO updateDriverStatus(Long id, String status, Integer statusCode) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with id: " + id));

        driver.setStatus(status);
        if (statusCode != null) {
            driver.setStatusCode(statusCode);
        }

        Driver updated = driverRepository.save(driver);
        return driverMapper.toResponseDTO(updated);
    }

    /**
     * Delete driver
     */
    @Transactional
    public void deleteDriver(Long id) {
        if (!driverRepository.existsById(id)) {
            throw new IllegalArgumentException("Driver not found with id: " + id);
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
        return driverRepository.findAvailableDrivers().stream()
                .anyMatch(driver -> driver.getIdDriver().equals(driverId));
    }
}