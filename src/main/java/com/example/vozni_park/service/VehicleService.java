package com.example.vozni_park.service;

import com.example.vozni_park.dto.request.VehicleRequestDTO;
import com.example.vozni_park.dto.response.VehicleResponseDTO;
import com.example.vozni_park.entity.*;
import com.example.vozni_park.mapper.VehicleMapper;
import com.example.vozni_park.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final VehicleModelRepository vehicleModelRepository;
    private final FuelTypeRepository fuelTypeRepository;
    private final RegistrationRepository registrationRepository;
    private final FirstAidKitRepository firstAidKitRepository;

    /**
     * Get all vehicles - returns DTOs to avoid lazy loading issues
     */
    public List<VehicleResponseDTO> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        return vehicleMapper.toResponseDTOList(vehicles);
    }

    /**
     * Get vehicle by ID
     */
    public Optional<VehicleResponseDTO> getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .map(vehicleMapper::toResponseDTO);
    }

    /**
     * Get vehicle by SAP number
     */
    public Optional<VehicleResponseDTO> getVehicleBySapNumber(Long sapNumber) {
        return vehicleRepository.findBySapNumber(sapNumber)
                .map(vehicleMapper::toResponseDTO);
    }

    /**
     * Get vehicle by chassis number
     */
    public Optional<VehicleResponseDTO> getVehicleByChassisNumber(String chassisNumber) {
        return vehicleRepository.findByChassisNumber(chassisNumber)
                .map(vehicleMapper::toResponseDTO);
    }

    /**
     * Get vehicles by status
     */
    public List<VehicleResponseDTO> getVehiclesByStatus(String status) {
        List<Vehicle> vehicles = vehicleRepository.findByVehicleStatus(status);
        return vehicleMapper.toResponseDTOList(vehicles);
    }

    /**
     * Get vehicles by location
     */
    public List<VehicleResponseDTO> getVehiclesByLocation(Long locationId) {
        List<Vehicle> vehicles = vehicleRepository.findByLocationId(locationId);
        return vehicleMapper.toResponseDTOList(vehicles);
    }

    /**
     * Get vehicles without location
     */
    public List<VehicleResponseDTO> getVehiclesWithoutLocation() {
        List<Vehicle> vehicles = vehicleRepository.findVehiclesWithoutLocation();
        return vehicleMapper.toResponseDTOList(vehicles);
    }

    /**
     * Get vehicles by model
     */
    public List<VehicleResponseDTO> getVehiclesByModel(Long modelId) {
        List<Vehicle> vehicles = vehicleRepository.findByVehicleModelId(modelId);
        return vehicleMapper.toResponseDTOList(vehicles);
    }

    /**
     * Get vehicles by fuel type
     */
    public List<VehicleResponseDTO> getVehiclesByFuelType(Long fuelTypeId) {
        List<Vehicle> vehicles = vehicleRepository.findByFuelTypeId(fuelTypeId);
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
     * Update existing vehicle from DTO
     */
    @Transactional
    public VehicleResponseDTO updateVehicle(Long id, VehicleRequestDTO vehicleDTO) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + id));

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
     * Update vehicle status
     */
    @Transactional
    public VehicleResponseDTO updateVehicleStatus(Long id, String status, Integer statusCode) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + id));

        vehicle.setVehicleStatus(status);
        if (statusCode != null) {
            vehicle.setStatusCode(statusCode);
        }

        Vehicle updated = vehicleRepository.save(vehicle);
        return vehicleMapper.toResponseDTO(updated);
    }

    /**
     * Delete vehicle
     */
    @Transactional
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehicle not found with id: " + id);
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
}