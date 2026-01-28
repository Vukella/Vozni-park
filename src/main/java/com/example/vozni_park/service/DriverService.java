package com.example.vozni_park.service;

import com.example.vozni_park.entity.Driver;
import com.example.vozni_park.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class DriverService {
    
    private final DriverRepository driverRepository;
    
    /**
     * Get all drivers
     */
    @Transactional(readOnly = true)
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }
    
    /**
     * Get driver by ID
     */
    @Transactional(readOnly = true)
    public Optional<Driver> getDriverById(Long id) {
        return driverRepository.findById(id);
    }
    
    /**
     * Get driver by SAP number
     */
    @Transactional(readOnly = true)
    public Optional<Driver> getDriverBySapNumber(Long sapNumber) {
        return driverRepository.findBySapNumber(sapNumber);
    }
    
    /**
     * Get drivers by status
     */
    @Transactional(readOnly = true)
    public List<Driver> getDriversByStatus(String status) {
        return driverRepository.findByStatus(status);
    }
    
    /**
     * Search drivers by name
     */
    @Transactional(readOnly = true)
    public List<Driver> searchDriversByName(String name) {
        return driverRepository.findByFullNameContainingIgnoreCase(name);
    }
    
    /**
     * Get drivers by location
     */
    @Transactional(readOnly = true)
    public List<Driver> getDriversByLocation(Long locationId) {
        return driverRepository.findByLocationId(locationId);
    }
    
    /**
     * Get drivers without location
     */
    @Transactional(readOnly = true)
    public List<Driver> getDriversWithoutLocation() {
        return driverRepository.findDriversWithoutLocation();
    }
    
    /**
     * Get available drivers (active and not on travel order)
     */
    @Transactional(readOnly = true)
    public List<Driver> getAvailableDrivers() {
        return driverRepository.findAvailableDrivers();
    }
    
    /**
     * Create new driver
     */
    public Driver createDriver(Driver driver) {
        // Validation: Check if SAP number already exists
        if (driverRepository.existsBySapNumber(driver.getSapNumber())) {
            throw new IllegalArgumentException("Driver with SAP number " + driver.getSapNumber() + " already exists");
        }
        
        // Validation: Validate phone format (basic check)
        if (driver.getPhone() != null && !driver.getPhone().matches("^\\+?[0-9]{10,15}$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        
        return driverRepository.save(driver);
    }
    
    /**
     * Update existing driver
     */
    public Driver updateDriver(Long id, Driver driverDetails) {
        Driver driver = driverRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Driver not found with id: " + id));
        
        // Validation: Check if new SAP number conflicts
        if (!driverDetails.getSapNumber().equals(driver.getSapNumber()) &&
            driverRepository.existsBySapNumber(driverDetails.getSapNumber())) {
            throw new IllegalArgumentException("Driver with SAP number " + driverDetails.getSapNumber() + " already exists");
        }
        
        // Validation: Validate phone format
        if (driverDetails.getPhone() != null && !driverDetails.getPhone().matches("^\\+?[0-9]{10,15}$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        
        // Update fields
        driver.setSapNumber(driverDetails.getSapNumber());
        driver.setFullName(driverDetails.getFullName());
        driver.setPhone(driverDetails.getPhone());
        driver.setStatus(driverDetails.getStatus());
        driver.setStatusCode(driverDetails.getStatusCode());
        
        return driverRepository.save(driver);
    }
    
    /**
     * Delete driver by ID
     */
    public void deleteDriver(Long id) {
        if (!driverRepository.existsById(id)) {
            throw new IllegalArgumentException("Driver not found with id: " + id);
        }
        
        // TODO: Check if driver is assigned to any active travel orders
        
        driverRepository.deleteById(id);
    }
    
    /**
     * Update driver status
     */
    public Driver updateDriverStatus(Long id, String status, Integer statusCode) {
        Driver driver = driverRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Driver not found with id: " + id));
        
        driver.setStatus(status);
        driver.setStatusCode(statusCode);
        
        return driverRepository.save(driver);
    }
    
    /**
     * Check if driver exists
     */
    @Transactional(readOnly = true)
    public boolean driverExists(Long id) {
        return driverRepository.existsById(id);
    }
    
    /**
     * Check if driver is available (active and not on travel order)
     */
    @Transactional(readOnly = true)
    public boolean isDriverAvailable(Long driverId) {
        return driverRepository.findAvailableDrivers().stream()
            .anyMatch(driver -> driver.getIdDriver().equals(driverId));
    }
}
