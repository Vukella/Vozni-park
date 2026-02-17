package com.example.vozni_park.service;

import com.example.vozni_park.dto.request.VehicleRequestDTO;
import com.example.vozni_park.dto.response.VehicleResponseDTO;
import com.example.vozni_park.entity.*;
import com.example.vozni_park.mapper.VehicleMapper;
import com.example.vozni_park.repository.*;
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
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final VehicleModelRepository vehicleModelRepository;
    private final FuelTypeRepository fuelTypeRepository;
    private final RegistrationRepository registrationRepository;
    private final FirstAidKitRepository firstAidKitRepository;
    private final LocationFilterService locationFilterService;

    /**
     * Get all vehicles - automatically filtered by user's location(s)
     */
    public List<VehicleResponseDTO> getAllVehicles() {
        List<Vehicle> vehicles;

        if (locationFilterService.isSuperAdmin()) {
            // SUPER_ADMIN sees everything
            log.debug("SUPER_ADMIN access - fetching all vehicles");
            vehicles = vehicleRepository.findAll();
        } else {
            // LOCAL_ADMIN sees only their location(s)
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            log.debug("LOCAL_ADMIN access - fetching vehicles for locations: {}", locationIds);
            vehicles = vehicleRepository.findByLocationIds(locationIds);
        }

        return vehicleMapper.toResponseDTOList(vehicles);
    }

    /**
     * Get vehicle by ID - validates location access
     */
    public Optional<VehicleResponseDTO> getVehicleById(Long id) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(id);

        // If not SUPER_ADMIN, validate location access
        if (vehicle.isPresent() && !locationFilterService.isSuperAdmin()) {
            Vehicle v = vehicle.get();
            if (v.getVehicleLocation() != null) {
                Long vehicleLocationId = v.getVehicleLocation().getLocationUnit().getIdLocationUnit();
                if (!locationFilterService.hasAccessToLocation(vehicleLocationId)) {
                    log.warn("User {} denied access to vehicle {} at location {}",
                            locationFilterService.getCurrentUsername(), id, vehicleLocationId);
                    return Optional.empty();
                }
            }
        }

        return vehicle.map(vehicleMapper::toResponseDTO);
    }

    /**
     * Get vehicle by SAP number - validates location access
     */
    public Optional<VehicleResponseDTO> getVehicleBySapNumber(Long sapNumber) {
        Optional<Vehicle> vehicle = vehicleRepository.findBySapNumber(sapNumber);

        // Validate location access
        if (vehicle.isPresent() && !locationFilterService.isSuperAdmin()) {
            Vehicle v = vehicle.get();
            if (v.getVehicleLocation() != null) {
                Long vehicleLocationId = v.getVehicleLocation().getLocationUnit().getIdLocationUnit();
                if (!locationFilterService.hasAccessToLocation(vehicleLocationId)) {
                    return Optional.empty();
                }
            }
        }

        return vehicle.map(vehicleMapper::toResponseDTO);
    }

    /**
     * Get vehicle by chassis number - validates location access
     */
    public Optional<VehicleResponseDTO> getVehicleByChassisNumber(String chassisNumber) {
        Optional<Vehicle> vehicle = vehicleRepository.findByChassisNumber(chassisNumber);

        // Validate location access
        if (vehicle.isPresent() && !locationFilterService.isSuperAdmin()) {
            Vehicle v = vehicle.get();
            if (v.getVehicleLocation() != null) {
                Long vehicleLocationId = v.getVehicleLocation().getLocationUnit().getIdLocationUnit();
                if (!locationFilterService.hasAccessToLocation(vehicleLocationId)) {
                    return Optional.empty();
                }
            }
        }

        return vehicle.map(vehicleMapper::toResponseDTO);
    }

    /**
     * Get vehicles by status - location filtered
     */
    public List<VehicleResponseDTO> getVehiclesByStatus(String status) {
        List<Vehicle> vehicles;

        if (locationFilterService.isSuperAdmin()) {
            vehicles = vehicleRepository.findByVehicleStatus(status);
        } else {
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            vehicles = vehicleRepository.findByLocationIdsAndStatus(locationIds, status);
        }

        return vehicleMapper.toResponseDTOList(vehicles);
    }

    /**
     * Get vehicles by location - validates location access
     */
    public List<VehicleResponseDTO> getVehiclesByLocation(Long locationId) {
        // Validate location access
        if (!locationFilterService.isSuperAdmin()) {
            locationFilterService.validateLocationAccess(locationId);
        }

        List<Vehicle> vehicles = vehicleRepository.findByLocationId(locationId);
        return vehicleMapper.toResponseDTOList(vehicles);
    }

    /**
     * Get vehicles without location - location filtered
     */
    public List<VehicleResponseDTO> getVehiclesWithoutLocation() {
        // Only SUPER_ADMIN can see unassigned vehicles
        if (!locationFilterService.isSuperAdmin()) {
            log.warn("LOCAL_ADMIN attempted to access unassigned vehicles");
            throw new SecurityException("Only SUPER_ADMIN can view vehicles without location assignment");
        }

        List<Vehicle> vehicles = vehicleRepository.findVehiclesWithoutLocation();
        return vehicleMapper.toResponseDTOList(vehicles);
    }

    /**
     * Get vehicles by model - location filtered
     */
    public List<VehicleResponseDTO> getVehiclesByModel(Long modelId) {
        List<Vehicle> vehicles;

        if (locationFilterService.isSuperAdmin()) {
            vehicles = vehicleRepository.findByVehicleModelId(modelId);
        } else {
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            vehicles = vehicleRepository.findByLocationIdsAndModel(locationIds, modelId);
        }

        return vehicleMapper.toResponseDTOList(vehicles);
    }

    /**
     * Get vehicles by fuel type - location filtered
     */
    public List<VehicleResponseDTO> getVehiclesByFuelType(Long fuelTypeId) {
        List<Vehicle> vehicles;

        if (locationFilterService.isSuperAdmin()) {
            vehicles = vehicleRepository.findByFuelTypeId(fuelTypeId);
        } else {
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            vehicles = vehicleRepository.findByLocationIdsAndFuelType(locationIds, fuelTypeId);
        }

        return vehicleMapper.toResponseDTOList(vehicles);
    }

    /**
     * Create new vehicle from DTO
     */
    @Transactional
    public VehicleResponseDTO createVehicle(VehicleRequestDTO vehicleDTO) {
        // Validation: Check if SAP number already exists
        if (vehicleDTO.getSapNumber() != null &&
                vehicleRepository.existsBySapNumber(vehicleDTO.getSapNumber())) {
            throw new IllegalArgumentException("Vehicle with SAP number " + vehicleDTO.getSapNumber() + " already exists");
        }

        // Validation: Check if chassis number already exists
        if (vehicleDTO.getChassisNumber() != null &&
                vehicleRepository.existsByChassisNumber(vehicleDTO.getChassisNumber())) {
            throw new IllegalArgumentException("Vehicle with chassis number '" + vehicleDTO.getChassisNumber() + "' already exists");
        }

        // Convert DTO to entity
        Vehicle vehicle = vehicleMapper.toEntity(vehicleDTO);

        // Set related entities from IDs
        setRelatedEntities(vehicle, vehicleDTO);

        // Save and return DTO
        Vehicle saved = vehicleRepository.save(vehicle);
        return vehicleMapper.toResponseDTO(saved);
    }

    /**
     * Update existing vehicle from DTO - validates location access
     */
    @Transactional
    public VehicleResponseDTO updateVehicle(Long id, VehicleRequestDTO vehicleDTO) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + id));

        // Validate location access for LOCAL_ADMIN
        if (!locationFilterService.isSuperAdmin() && vehicle.getVehicleLocation() != null) {
            Long vehicleLocationId = vehicle.getVehicleLocation().getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(vehicleLocationId);
        }

        // Validation: Check if new SAP number conflicts
        if (vehicleDTO.getSapNumber() != null &&
                !vehicleDTO.getSapNumber().equals(vehicle.getSapNumber()) &&
                vehicleRepository.existsBySapNumber(vehicleDTO.getSapNumber())) {
            throw new IllegalArgumentException("Vehicle with SAP number " + vehicleDTO.getSapNumber() + " already exists");
        }

        // Validation: Check if new chassis number conflicts
        if (vehicleDTO.getChassisNumber() != null &&
                !vehicleDTO.getChassisNumber().equals(vehicle.getChassisNumber()) &&
                vehicleRepository.existsByChassisNumber(vehicleDTO.getChassisNumber())) {
            throw new IllegalArgumentException("Vehicle with chassis number '" + vehicleDTO.getChassisNumber() + "' already exists");
        }

        // Update entity from DTO
        vehicleMapper.updateEntity(vehicle, vehicleDTO);

        // Update related entities
        setRelatedEntities(vehicle, vehicleDTO);

        // Save and return DTO
        Vehicle updated = vehicleRepository.save(vehicle);
        return vehicleMapper.toResponseDTO(updated);
    }

    /**
     * Update vehicle status - validates location access
     */
    @Transactional
    public VehicleResponseDTO updateVehicleStatus(Long id, String status, Integer statusCode) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + id));

        // Validate location access
        if (!locationFilterService.isSuperAdmin() && vehicle.getVehicleLocation() != null) {
            Long vehicleLocationId = vehicle.getVehicleLocation().getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(vehicleLocationId);
        }

        vehicle.setVehicleStatus(status);
        if (statusCode != null) {
            vehicle.setStatusCode(statusCode);
        }

        Vehicle updated = vehicleRepository.save(vehicle);
        return vehicleMapper.toResponseDTO(updated);
    }

    /**
     * Delete vehicle - validates location access
     */
    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + id));

        // Validate location access
        if (!locationFilterService.isSuperAdmin() && vehicle.getVehicleLocation() != null) {
            Long vehicleLocationId = vehicle.getVehicleLocation().getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(vehicleLocationId);
        }

        vehicleRepository.deleteById(id);
    }

    /**
     * Check if vehicle exists
     */
    public boolean vehicleExists(Long id) {
        return vehicleRepository.existsById(id);
    }

    /**
     * Helper method to set related entities from IDs in DTO
     */
    private void setRelatedEntities(Vehicle vehicle, VehicleRequestDTO dto) {
        // Set Registration (optional)
        if (dto.getRegistrationId() != null) {
            Registration registration = registrationRepository.findById(dto.getRegistrationId())
                    .orElseThrow(() -> new IllegalArgumentException("Registration with ID " + dto.getRegistrationId() + " not found"));
            vehicle.setRegistration(registration);
        }

        // Set FuelType (required)
        FuelType fuelType = fuelTypeRepository.findById(dto.getFuelTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Fuel type with ID " + dto.getFuelTypeId() + " not found"));
        vehicle.setFuelType(fuelType);

        // Set FirstAidKit (optional)
        if (dto.getFirstAidKitId() != null) {
            FirstAidKit firstAidKit = firstAidKitRepository.findById(dto.getFirstAidKitId())
                    .orElseThrow(() -> new IllegalArgumentException("First aid kit with ID " + dto.getFirstAidKitId() + " not found"));
            vehicle.setFirstAidKit(firstAidKit);
        }

        // Set VehicleModel (required)
        VehicleModel vehicleModel = vehicleModelRepository.findById(dto.getVehicleModelId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle model with ID " + dto.getVehicleModelId() + " not found"));
        vehicle.setVehicleModel(vehicleModel);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getAvailableVehicles() {
        List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
        List<Vehicle> vehicles = (locationIds == null || locationIds.isEmpty())
                ? vehicleRepository.findAvailableVehicles()
                : vehicleRepository.findAvailableVehiclesByLocations(locationIds);
        return vehicleMapper.toResponseDTOList(vehicles);
    }
}