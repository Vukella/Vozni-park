package com.example.vozni_park.service;

import com.example.vozni_park.dto.response.VehicleLocationResponseDTO;
import com.example.vozni_park.entity.VehicleLocation;
import com.example.vozni_park.mapper.VehicleLocationMapper;
import com.example.vozni_park.repository.LocationUnitRepository;
import com.example.vozni_park.repository.VehicleLocationRepository;
import com.example.vozni_park.repository.VehicleRepository;
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
public class VehicleLocationService {

    private final VehicleLocationMapper vehicleLocationMapper;
    private final VehicleLocationRepository vehicleLocationRepository;
    private final VehicleRepository vehicleRepository;
    private final LocationUnitRepository locationUnitRepository;
    private final LocationFilterService locationFilterService;

    public List<VehicleLocationResponseDTO> getAllVehicleLocations() {
        if (locationFilterService.isSuperAdmin()) {
            return vehicleLocationMapper.toResponseDTOList(vehicleLocationRepository.findAll());
        } else {
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            return vehicleLocationMapper.toResponseDTOList(
                    vehicleLocationRepository.findByLocationUnitIdIn(locationIds));
        }
    }

    public Optional<VehicleLocationResponseDTO> getVehicleLocationById(Long id) {
        Optional<VehicleLocation> vehicleLocation = vehicleLocationRepository.findById(id);

        if (vehicleLocation.isPresent() && !locationFilterService.isSuperAdmin()) {
            Long locationId = vehicleLocation.get().getLocationUnit().getIdLocationUnit();
            if (!locationFilterService.hasAccessToLocation(locationId)) {
                log.warn("User {} denied access to vehicle location assignment {} at location {}",
                        locationFilterService.getCurrentUsername(), id, locationId);
                return Optional.empty();
            }
        }

        return vehicleLocation.map(vehicleLocationMapper::toResponseDTO);
    }

    public Optional<VehicleLocationResponseDTO> getVehicleLocationByVehicleId(Long vehicleId) {
        Optional<VehicleLocation> vehicleLocation = vehicleLocationRepository.findByVehicleId(vehicleId);

        if (vehicleLocation.isPresent() && !locationFilterService.isSuperAdmin()) {
            Long locationId = vehicleLocation.get().getLocationUnit().getIdLocationUnit();
            if (!locationFilterService.hasAccessToLocation(locationId)) {
                return Optional.empty();
            }
        }

        return vehicleLocation.map(vehicleLocationMapper::toResponseDTO);
    }

    public List<VehicleLocationResponseDTO> getVehicleLocationsByLocationUnitId(Long locationUnitId) {
        if (!locationFilterService.isSuperAdmin()) {
            locationFilterService.validateLocationAccess(locationUnitId);
        }
        return vehicleLocationMapper.toResponseDTOList(
                vehicleLocationRepository.findByLocationUnitId(locationUnitId));
    }

    @Transactional
    public VehicleLocationResponseDTO assignVehicleToLocation(Long vehicleId, Long locationUnitId) {
        if (!locationFilterService.isSuperAdmin()) {
            locationFilterService.validateLocationAccess(locationUnitId);
        }

        if (!vehicleRepository.existsById(vehicleId)) {
            throw new IllegalArgumentException("Vehicle not found with id: " + vehicleId);
        }

        if (!locationUnitRepository.existsById(locationUnitId)) {
            throw new IllegalArgumentException("Location not found with id: " + locationUnitId);
        }

        Optional<VehicleLocation> existing = vehicleLocationRepository.findByVehicleId(vehicleId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Vehicle " + vehicleId + " is already assigned to a location. Use update instead.");
        }

        VehicleLocation vehicleLocation = new VehicleLocation();
        vehicleLocation.setVehicleId(vehicleId);
        vehicleLocation.setLocationUnitId(locationUnitId);

        return vehicleLocationMapper.toResponseDTO(vehicleLocationRepository.save(vehicleLocation));
    }

    @Transactional
    public VehicleLocationResponseDTO updateVehicleLocation(Long id, Long newLocationUnitId) {
        VehicleLocation vehicleLocation = vehicleLocationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehicle location assignment not found with id: " + id));

        if (!locationFilterService.isSuperAdmin()) {
            Long currentLocationId = vehicleLocation.getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(currentLocationId);
            locationFilterService.validateLocationAccess(newLocationUnitId);
        }

        if (!locationUnitRepository.existsById(newLocationUnitId)) {
            throw new IllegalArgumentException("Location not found with id: " + newLocationUnitId);
        }

        vehicleLocation.setLocationUnitId(newLocationUnitId);
        return vehicleLocationMapper.toResponseDTO(vehicleLocationRepository.save(vehicleLocation));
    }

    @Transactional
    public void deleteVehicleLocation(Long id) {
        VehicleLocation vehicleLocation = vehicleLocationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehicle location assignment not found with id: " + id));

        if (!locationFilterService.isSuperAdmin()) {
            Long locationId = vehicleLocation.getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(locationId);
        }

        vehicleLocationRepository.deleteById(id);
    }
}