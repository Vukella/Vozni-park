package com.example.vozni_park.service;

import com.example.vozni_park.entity.DriverLocation;
import com.example.vozni_park.repository.DriverLocationRepository;
import com.example.vozni_park.repository.DriverRepository;
import com.example.vozni_park.repository.LocationUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class DriverLocationService {
    
    private final DriverLocationRepository driverLocationRepository;
    private final DriverRepository driverRepository;
    private final LocationUnitRepository locationUnitRepository;
    
    @Transactional(readOnly = true)
    public List<DriverLocation> getAllDriverLocations() {
        return driverLocationRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<DriverLocation> getDriverLocationById(Long id) {
        return driverLocationRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<DriverLocation> getDriverLocationByDriverId(Long driverId) {
        return driverLocationRepository.findByDriverId(driverId);
    }
    
    @Transactional(readOnly = true)
    public List<DriverLocation> getDriverLocationsByLocationUnitId(Long locationUnitId) {
        return driverLocationRepository.findByLocationUnitId(locationUnitId);
    }
    
    public DriverLocation assignDriverToLocation(Long driverId, Long locationUnitId) {
        if (!driverRepository.existsById(driverId)) {
            throw new IllegalArgumentException("Driver not found with id: " + driverId);
        }
        if (!locationUnitRepository.existsById(locationUnitId)) {
            throw new IllegalArgumentException("Location unit not found with id: " + locationUnitId);
        }
        if (driverLocationRepository.existsByDriverId(driverId)) {
            throw new IllegalArgumentException("Driver is already assigned to a location");
        }
        
        DriverLocation driverLocation = new DriverLocation();
        driverLocation.setDriverId(driverId);
        driverLocation.setLocationUnitId(locationUnitId);
        
        return driverLocationRepository.save(driverLocation);
    }
    
    public DriverLocation updateDriverLocation(Long id, Long locationUnitId) {
        DriverLocation driverLocation = driverLocationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Driver location not found with id: " + id));
        
        if (!locationUnitRepository.existsById(locationUnitId)) {
            throw new IllegalArgumentException("Location unit not found with id: " + locationUnitId);
        }
        
        driverLocation.setLocationUnitId(locationUnitId);
        
        return driverLocationRepository.save(driverLocation);
    }
    
    public void deleteDriverLocation(Long id) {
        if (!driverLocationRepository.existsById(id)) {
            throw new IllegalArgumentException("Driver location not found with id: " + id);
        }
        driverLocationRepository.deleteById(id);
    }
}
