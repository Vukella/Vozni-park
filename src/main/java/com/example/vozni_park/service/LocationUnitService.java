package com.example.vozni_park.service;

import com.example.vozni_park.entity.LocationUnit;
import com.example.vozni_park.repository.LocationUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class LocationUnitService {
    
    private final LocationUnitRepository locationUnitRepository;
    
    /**
     * Get all location units
     */
    @Transactional(readOnly = true)
    public List<LocationUnit> getAllLocationUnits() {
        return locationUnitRepository.findAll();
    }
    
    /**
     * Get location unit by ID
     */
    @Transactional(readOnly = true)
    public Optional<LocationUnit> getLocationUnitById(Long id) {
        return locationUnitRepository.findById(id);
    }
    
    /**
     * Get location unit by name
     */
    @Transactional(readOnly = true)
    public Optional<LocationUnit> getLocationUnitByName(String locationName) {
        return locationUnitRepository.findByLocationName(locationName);
    }
    
    /**
     * Create new location unit
     */
    public LocationUnit createLocationUnit(LocationUnit locationUnit) {
        // Validation: Check if location with same name already exists
        if (locationUnitRepository.existsByLocationName(locationUnit.getLocationName())) {
            throw new IllegalArgumentException("Location with name '" + locationUnit.getLocationName() + "' already exists");
        }
        
        return locationUnitRepository.save(locationUnit);
    }
    
    /**
     * Update existing location unit
     */
    public LocationUnit updateLocationUnit(Long id, LocationUnit locationDetails) {
        LocationUnit location = locationUnitRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Location unit not found with id: " + id));
        
        // Check if new name conflicts
        if (!location.getLocationName().equals(locationDetails.getLocationName()) && 
            locationUnitRepository.existsByLocationName(locationDetails.getLocationName())) {
            throw new IllegalArgumentException("Location with name '" + locationDetails.getLocationName() + "' already exists");
        }
        
        location.setLocationName(locationDetails.getLocationName());
        location.setLocationAddress(locationDetails.getLocationAddress());
        
        return locationUnitRepository.save(location);
    }
    
    /**
     * Delete location unit by ID
     */
    public void deleteLocationUnit(Long id) {
        if (!locationUnitRepository.existsById(id)) {
            throw new IllegalArgumentException("Location unit not found with id: " + id);
        }
        
        // TODO: Check if location has associated vehicles, drivers, or users before deleting
        
        locationUnitRepository.deleteById(id);
    }
    
    /**
     * Check if location unit exists
     */
    @Transactional(readOnly = true)
    public boolean locationUnitExists(Long id) {
        return locationUnitRepository.existsById(id);
    }
}
