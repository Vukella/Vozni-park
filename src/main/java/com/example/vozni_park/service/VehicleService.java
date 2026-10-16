package com.example.vozni_park.service;

import com.example.vozni_park.dto.request.VehicleRequestDTO;
import com.example.vozni_park.dto.response.VehicleResponseDTO;
import com.example.vozni_park.entity.*;
import com.example.vozni_park.exception.BusinessRuleViolationException;
import com.example.vozni_park.exception.DuplicateEntityException;
import com.example.vozni_park.exception.EntityNotFoundException;
import com.example.vozni_park.exception.LocationAccessDeniedException;
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
    private final StatusCalculationService statusCalculationService;

    public List<VehicleResponseDTO> getAllVehicles() {
        List<Vehicle> vehicles;
        if (locationFilterService.isSuperAdmin()) {
            vehicles = vehicleRepository.findAll();
        } else {
            List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
            vehicles = vehicleRepository.findByLocationIds(locationIds);
        }
        return vehicleMapper.toResponseDTOList(vehicles);
    }

    public Optional<VehicleResponseDTO> getVehicleById(Long id) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(id);
        if (vehicle.isPresent() && !locationFilterService.isSuperAdmin()) {
            Vehicle v = vehicle.get();
            if (v.getVehicleLocation() != null) {
                Long locationId = v.getVehicleLocation().getLocationUnit().getIdLocationUnit();
                if (!locationFilterService.hasAccessToLocation(locationId)) {
                    return Optional.empty();
                }
            }
        }
        return vehicle.map(vehicleMapper::toResponseDTO);
    }

    public Optional<VehicleResponseDTO> getVehicleBySapNumber(Long sapNumber) {
        Optional<Vehicle> vehicle = vehicleRepository.findBySapNumber(sapNumber);
        if (vehicle.isPresent() && !locationFilterService.isSuperAdmin()) {
            Vehicle v = vehicle.get();
            if (v.getVehicleLocation() != null) {
                Long locationId = v.getVehicleLocation().getLocationUnit().getIdLocationUnit();
                if (!locationFilterService.hasAccessToLocation(locationId)) {
                    return Optional.empty();
                }
            }
        }
        return vehicle.map(vehicleMapper::toResponseDTO);
    }

    public Optional<VehicleResponseDTO> getVehicleByChassisNumber(String chassisNumber) {
        Optional<Vehicle> vehicle = vehicleRepository.findByChassisNumber(chassisNumber);
        if (vehicle.isPresent() && !locationFilterService.isSuperAdmin()) {
            Vehicle v = vehicle.get();
            if (v.getVehicleLocation() != null) {
                Long locationId = v.getVehicleLocation().getLocationUnit().getIdLocationUnit();
                if (!locationFilterService.hasAccessToLocation(locationId)) {
                    return Optional.empty();
                }
            }
        }
        return vehicle.map(vehicleMapper::toResponseDTO);
    }

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

    public List<VehicleResponseDTO> getVehiclesByLocation(Long locationId) {
        if (!locationFilterService.isSuperAdmin()) {
            locationFilterService.validateLocationAccess(locationId);
        }
        return vehicleMapper.toResponseDTOList(vehicleRepository.findByLocationId(locationId));
    }

    public List<VehicleResponseDTO> getVehiclesWithoutLocation() {
        if (!locationFilterService.isSuperAdmin()) {
            throw new LocationAccessDeniedException("Only SUPER_ADMIN can view vehicles without location assignment");
        }
        return vehicleMapper.toResponseDTOList(vehicleRepository.findVehiclesWithoutLocation());
    }

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

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getAvailableVehicles() {
        List<Long> locationIds = locationFilterService.getCurrentUserLocationIds();
        List<Vehicle> vehicles = (locationIds == null || locationIds.isEmpty())
                ? vehicleRepository.findAvailableVehicles()
                : vehicleRepository.findAvailableVehiclesByLocations(locationIds);
        return vehicleMapper.toResponseDTOList(vehicles);
    }

    @Transactional
    public VehicleResponseDTO createVehicle(VehicleRequestDTO vehicleDTO) {
        if (vehicleDTO.getSapNumber() != null &&
                vehicleRepository.existsBySapNumber(vehicleDTO.getSapNumber())) {
            throw new DuplicateEntityException("Vehicle", "SAP number", vehicleDTO.getSapNumber());
        }
        if (vehicleDTO.getChassisNumber() != null &&
                vehicleRepository.existsByChassisNumber(vehicleDTO.getChassisNumber())) {
            throw new DuplicateEntityException("Vehicle", "chassis number", vehicleDTO.getChassisNumber());
        }

        Vehicle vehicle = vehicleMapper.toEntity(vehicleDTO);
        setRelatedEntities(vehicle, vehicleDTO);

        // Experiment A: populate denormalized display name
        vehicle.setVehicleDisplayName(buildDisplayName(vehicle));

        Vehicle saved = vehicleRepository.save(vehicle);

        // Set initial status after save so we have the ID
        statusCalculationService.recalculateAndSaveVehicleStatus(saved.getIdVehicle());

        return vehicleMapper.toResponseDTO(vehicleRepository.findById(saved.getIdVehicle()).orElseThrow());
    }

    @Transactional
    public VehicleResponseDTO updateVehicle(Long id, VehicleRequestDTO vehicleDTO) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", id));

        if (!locationFilterService.isSuperAdmin() && vehicle.getVehicleLocation() != null) {
            Long locationId = vehicle.getVehicleLocation().getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(locationId);
        }

        if (vehicleDTO.getSapNumber() != null &&
                !vehicleDTO.getSapNumber().equals(vehicle.getSapNumber()) &&
                vehicleRepository.existsBySapNumber(vehicleDTO.getSapNumber())) {
            throw new DuplicateEntityException("Vehicle", "SAP number", vehicleDTO.getSapNumber());
        }
        if (vehicleDTO.getChassisNumber() != null &&
                !vehicleDTO.getChassisNumber().equals(vehicle.getChassisNumber()) &&
                vehicleRepository.existsByChassisNumber(vehicleDTO.getChassisNumber())) {
            throw new DuplicateEntityException("Vehicle", "chassis number", vehicleDTO.getChassisNumber());
        }

        vehicleMapper.updateEntity(vehicle, vehicleDTO);
        setRelatedEntities(vehicle, vehicleDTO);

        // Experiment A: recalculate display name after update (brand/model/registration may have changed)
        vehicle.setVehicleDisplayName(buildDisplayName(vehicle));

        vehicleRepository.save(vehicle);

        // Document changes may have changed status
        statusCalculationService.recalculateAndSaveVehicleStatus(id);

        return vehicleMapper.toResponseDTO(vehicleRepository.findById(id).orElseThrow());
    }

    @Transactional
    public void decommissionVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", id));

        if (!locationFilterService.isSuperAdmin() && vehicle.getVehicleLocation() != null) {
            Long locationId = vehicle.getVehicleLocation().getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(locationId);
        }

        if (Integer.valueOf(1).equals(vehicle.getDecommissioned())) {
            throw new BusinessRuleViolationException("ALREADY_DECOMMISSIONED",
                    "Vehicle is already decommissioned");
        }

        vehicle.setDecommissioned(1);
        vehicle.setDecommissionDate(java.time.LocalDate.now());
        vehicleRepository.save(vehicle);
        log.info("Vehicle {} decommissioned", id);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", id));

        if (!locationFilterService.isSuperAdmin() && vehicle.getVehicleLocation() != null) {
            Long locationId = vehicle.getVehicleLocation().getLocationUnit().getIdLocationUnit();
            locationFilterService.validateLocationAccess(locationId);
        }

        vehicleRepository.deleteById(id);
    }

    public boolean vehicleExists(Long id) {
        return vehicleRepository.existsById(id);
    }

    // ── Private helpers ───────────────────────────────────────────────

    private void setRelatedEntities(Vehicle vehicle, VehicleRequestDTO dto) {
        if (dto.getRegistrationId() != null) {
            Registration registration = registrationRepository.findById(dto.getRegistrationId())
                    .orElseThrow(() -> new EntityNotFoundException("Registration", dto.getRegistrationId()));
            vehicle.setRegistration(registration);
        }
        FuelType fuelType = fuelTypeRepository.findById(dto.getFuelTypeId())
                .orElseThrow(() -> new EntityNotFoundException("FuelType", dto.getFuelTypeId()));
        vehicle.setFuelType(fuelType);

        if (dto.getFirstAidKitId() != null) {
            FirstAidKit firstAidKit = firstAidKitRepository.findById(dto.getFirstAidKitId())
                    .orElseThrow(() -> new EntityNotFoundException("FirstAidKit", dto.getFirstAidKitId()));
            vehicle.setFirstAidKit(firstAidKit);
        }
        VehicleModel vehicleModel = vehicleModelRepository.findById(dto.getVehicleModelId())
                .orElseThrow(() -> new EntityNotFoundException("VehicleModel", dto.getVehicleModelId()));
        vehicle.setVehicleModel(vehicleModel);
    }

    /**
     * Builds the denormalized display name for a vehicle.
     * Format: "Brand Model (REG-NUMBER)" — e.g. "Škoda Octavia (BG-1234-AA)".
     * Registration suffix is omitted if the vehicle has no registration assigned.
     * Called by createVehicle() and updateVehicle() to keep vehicle_display_name in sync.
     */
    private String buildDisplayName(Vehicle vehicle) {
        StringBuilder sb = new StringBuilder();

        if (vehicle.getVehicleModel() != null) {
            if (vehicle.getVehicleModel().getBrand() != null) {
                sb.append(vehicle.getVehicleModel().getBrand().getName()).append(" ");
            }
            sb.append(vehicle.getVehicleModel().getName());
        }

        if (vehicle.getRegistration() != null
                && vehicle.getRegistration().getRegistrationNumber() != null
                && !vehicle.getRegistration().getRegistrationNumber().isBlank()) {
            sb.append(" (").append(vehicle.getRegistration().getRegistrationNumber()).append(")");
        }

        return sb.toString().trim();
    }
}
