package com.example.vozni_park.service;

import com.example.vozni_park.dto.response.LocationUnitResponseDTO;
import com.example.vozni_park.entity.LocationUnit;
import com.example.vozni_park.mapper.LocationUnitMapper;
import com.example.vozni_park.repository.LocationUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationUnitService {

    private final LocationUnitRepository locationUnitRepository;
    private final LocationUnitMapper locationUnitMapper;

    /**
     * Get all location units - returns DTOs
     */
    public List<LocationUnitResponseDTO> getAllLocationUnits() {
        List<LocationUnit> locations = locationUnitRepository.findAll();
        return locationUnitMapper.toResponseDTOList(locations);
    }

    /**
     * Get location unit by ID
     */
    public Optional<LocationUnitResponseDTO> getLocationUnitById(Long id) {
        return locationUnitRepository.findById(id)
                .map(locationUnitMapper::toResponseDTO);
    }

    /**
     * Get location unit by name
     */
    public Optional<LocationUnitResponseDTO> getLocationUnitByName(String locationName) {
        return locationUnitRepository.findByLocationName(locationName)
                .map(locationUnitMapper::toResponseDTO);
    }

    /**
     * Create new location unit
     */
    @Transactional
    public LocationUnitResponseDTO createLocationUnit(LocationUnit locationUnit) {
        // Validation: Check if location with same name already exists
        if (locationUnitRepository.existsByLocationName(locationUnit.getLocationName())) {
            throw new IllegalArgumentException("Location with name '" + locationUnit.getLocationName() + "' already exists");
        }

        LocationUnit saved = locationUnitRepository.save(locationUnit);
        return locationUnitMapper.toResponseDTO(saved);
    }

    /**
     * Update existing location unit
     */
    @Transactional
    public LocationUnitResponseDTO updateLocationUnit(Long id, LocationUnit locationDetails) {
        LocationUnit location = locationUnitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location unit not found with id: " + id));

        // Check if new name conflicts
        if (!location.getLocationName().equals(locationDetails.getLocationName()) &&
                locationUnitRepository.existsByLocationName(locationDetails.getLocationName())) {
            throw new IllegalArgumentException("Location with name '" + locationDetails.getLocationName() + "' already exists");
        }

        location.setLocationName(locationDetails.getLocationName());
        location.setLocationAddress(locationDetails.getLocationAddress());

        LocationUnit updated = locationUnitRepository.save(location);
        return locationUnitMapper.toResponseDTO(updated);
    }

    /**
     * Delete location unit by ID
     */
    @Transactional
    public void deleteLocationUnit(Long id) {
        if (!locationUnitRepository.existsById(id)) {
            throw new IllegalArgumentException("Location unit not found with id: " + id);
        }
        locationUnitRepository.deleteById(id);
    }

    /**
     * Check if location unit exists
     */
    public boolean locationUnitExists(Long id) {
        return locationUnitRepository.existsById(id);
    }
}