package com.example.vozni_park.service;

import com.example.vozni_park.dto.response.DriverLocationResponseDTO;
import com.example.vozni_park.entity.DriverLocation;
import com.example.vozni_park.mapper.DriverLocationMapper;
import com.example.vozni_park.repository.DriverLocationRepository;
import com.example.vozni_park.repository.DriverRepository;
import com.example.vozni_park.repository.LocationUnitRepository;
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
public class DriverLocationService {

    private final DriverLocationMapper driverLocationMapper;
    private final DriverLocationRepository driverLocationRepository;
    private final DriverRepository driverRepository;
    private final LocationUnitRepository locationUnitRepository;
    private final LocationFilterService locationFilterService;

    public List<DriverLocationResponseDTO> getAllDriverLocations() {
        if (locationFilterService.isSuperAdmin()) {
            return driverLocationMapper.toResponseDTOList(driverLocationRepository.findAll());
        } else {
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            return driverLocationMapper.toResponseDTOList(
                    driverLocationRepository.findByLocationUnitIdIn(locationIds));
        }
    }

    public Optional<DriverLocationResponseDTO> getDriverLocationById(Long id) {
        Optional<DriverLocation> driverLocation = driverLocationRepository.findById(id);

        if (driverLocation.isPresent() && !locationFilterService.isSuperAdmin()) {
            Long locationId = driverLocation.get().getLocationUnit().getIdLocationUnit();
            if (!locationFilterService.hasAccessToLocation(locationId)) {
                log.warn("User {} denied access to driver location assignment {} at location {}",
                        locationFilterService.getCurrentUsername(), id, locationId);
                return Optional.empty();
            }
        }

        return driverLocation.map(driverLocationMapper::toResponseDTO);
    }

    public Optional<DriverLocationResponseDTO> getDriverLocationByDriverId(Long driverId) {
        Optional<DriverLocation> driverLocation = driverLocationRepository.findByDriverId(driverId);

        if (driverLocation.isPresent() && !locationFilterService.isSuperAdmin()) {
            Long locationId = driverLocation.get().getLocationUnit().getIdLocationUnit();
            if (!locationFilterService.hasAccessToLocation(locationId)) {
                return Optional.empty();
            }
        }

        return driverLocation.map(driverLocationMapper::toResponseDTO);
    }

    public List<DriverLocationResponseDTO> getDriverLocationsByLocationUnitId(Long locationUnitId) {
        if (!locationFilterService.isSuperAdmin()) {
            locationFilterService.validateLocationAccess(locationUnitId);
        }
        return driverLocationMapper.toResponseDTOList(
                driverLocationRepository.findByLocationUnitId(locationUnitId));
    }

    @Transactional
    public DriverLocationResponseDTO assignDriverToLocation(Long driverId, Long locationUnitId) {
        if (!locationFilterService.isSuperAdmin()) {
            locationFilterService.validateLocationAccess(locationUnitId);
        }

        if (!driverRepository.existsById(driverId)) {
            throw new IllegalArgumentException("Driver not found with id: " + driverId);
        }

        if (!locationUnitRepository.existsById(locationUnitId)) {
            throw new IllegalArgumentException("Location not found with id: " + locationUnitId);
        }

        Optional<DriverLocation> existing = driverLocationRepository.findByDriverId(driverId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Driver " + driverId + " is already assigned to a location. Use update instead.");
        }

        DriverLocation driverLocation = new DriverLocation();
        driverLocation.setDriverId(driverId);
        driverLocation.setLocationUnitId(locationUnitId);

        return driverLocationMapper.toResponseDTO(driverLocationRepository.save(driverLocation));
    }

    @Transactional
    public DriverLocationResponseDTO updateDriverLocation(Long id, Long newLocationUnitId) {
        DriverLocation driverLocation = driverLocationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Driver location assignment not found with id: " + id));

        if (!locationFilterService.isSuperAdmin()) {
            Long currentLocationId = driverLocation.getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(currentLocationId);
            locationFilterService.validateLocationAccess(newLocationUnitId);
        }

        if (!locationUnitRepository.existsById(newLocationUnitId)) {
            throw new IllegalArgumentException("Location not found with id: " + newLocationUnitId);
        }

        driverLocation.setLocationUnitId(newLocationUnitId);
        return driverLocationMapper.toResponseDTO(driverLocationRepository.save(driverLocation));
    }

    @Transactional
    public void deleteDriverLocation(Long id) {
        DriverLocation driverLocation = driverLocationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Driver location assignment not found with id: " + id));

        if (!locationFilterService.isSuperAdmin()) {
            Long locationId = driverLocation.getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(locationId);
        }

        driverLocationRepository.deleteById(id);
    }
}