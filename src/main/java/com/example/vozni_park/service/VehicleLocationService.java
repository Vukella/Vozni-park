package com.example.vozni_park.service;

import com.example.vozni_park.entity.VehicleLocation;
import com.example.vozni_park.repository.LocationUnitRepository;
import com.example.vozni_park.repository.VehicleLocationRepository;
import com.example.vozni_park.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class VehicleLocationService {
    
    private final VehicleLocationRepository vehicleLocationRepository;
    private final VehicleRepository vehicleRepository;
    private final LocationUnitRepository locationUnitRepository;
    
    @Transactional(readOnly = true)
    public List<VehicleLocation> getAllVehicleLocations() {
        return vehicleLocationRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<VehicleLocation> getVehicleLocationById(Long id) {
        return vehicleLocationRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<VehicleLocation> getVehicleLocationByVehicleId(Long vehicleId) {
        return vehicleLocationRepository.findByVehicleId(vehicleId);
    }
    
    @Transactional(readOnly = true)
    public List<VehicleLocation> getVehicleLocationsByLocationUnitId(Long locationUnitId) {
        return vehicleLocationRepository.findByLocationUnitId(locationUnitId);
    }
    
    public VehicleLocation assignVehicleToLocation(Long vehicleId, Long locationUnitId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new IllegalArgumentException("Vehicle not found with id: " + vehicleId);
        }
        if (!locationUnitRepository.existsById(locationUnitId)) {
            throw new IllegalArgumentException("Location unit not found with id: " + locationUnitId);
        }
        if (vehicleLocationRepository.existsByVehicleId(vehicleId)) {
            throw new IllegalArgumentException("Vehicle is already assigned to a location");
        }
        
        VehicleLocation vehicleLocation = new VehicleLocation();
        vehicleLocation.setVehicleId(vehicleId);
        vehicleLocation.setLocationUnitId(locationUnitId);
        
        return vehicleLocationRepository.save(vehicleLocation);
    }
    
    public VehicleLocation updateVehicleLocation(Long id, Long locationUnitId) {
        VehicleLocation vehicleLocation = vehicleLocationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Vehicle location not found with id: " + id));
        
        if (!locationUnitRepository.existsById(locationUnitId)) {
            throw new IllegalArgumentException("Location unit not found with id: " + locationUnitId);
        }
        
        vehicleLocation.setLocationUnitId(locationUnitId);
        
        return vehicleLocationRepository.save(vehicleLocation);
    }
    
    public void deleteVehicleLocation(Long id) {
        if (!vehicleLocationRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehicle location not found with id: " + id);
        }
        vehicleLocationRepository.deleteById(id);
    }
}
