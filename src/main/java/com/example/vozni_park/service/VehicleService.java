package com.example.vozni_park.service;

import com.example.vozni_park.entity.Vehicle;
import com.example.vozni_park.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class VehicleService {
    
    private final VehicleRepository vehicleRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final FuelTypeRepository fuelTypeRepository;
    private final RegistrationRepository registrationRepository;
    private final FirstAidKitRepository firstAidKitRepository;
    
    /**
     * Get all vehicles
     */
    @Transactional(readOnly = true)
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
    
    /**
     * Get vehicle by ID
     */
    @Transactional(readOnly = true)
    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }
    
    /**
     * Get vehicle by SAP number
     */
    @Transactional(readOnly = true)
    public Optional<Vehicle> getVehicleBySapNumber(Long sapNumber) {
        return vehicleRepository.findBySapNumber(sapNumber);
    }
    
    /**
     * Get vehicle by chassis number
     */
    @Transactional(readOnly = true)
    public Optional<Vehicle> getVehicleByChassisNumber(String chassisNumber) {
        return vehicleRepository.findByChassisNumber(chassisNumber);
    }
    
    /**
     * Get vehicles by status
     */
    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesByStatus(String status) {
        return vehicleRepository.findByVehicleStatus(status);
    }
    
    /**
     * Get vehicles by location
     */
    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesByLocation(Long locationId) {
        return vehicleRepository.findByLocationId(locationId);
    }
    
    /**
     * Get vehicles without location
     */
    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesWithoutLocation() {
        return vehicleRepository.findVehiclesWithoutLocation();
    }
    
    /**
     * Get vehicles by model
     */
    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesByModel(Long modelId) {
        return vehicleRepository.findByVehicleModelId(modelId);
    }
    
    /**
     * Get vehicles by fuel type
     */
    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesByFuelType(Long fuelTypeId) {
        return vehicleRepository.findByFuelTypeId(fuelTypeId);
    }
    
    /**
     * Create new vehicle
     */
    public Vehicle createVehicle(Vehicle vehicle) {
        // Validation: Check if SAP number already exists
        if (vehicle.getSapNumber() != null && vehicleRepository.existsBySapNumber(vehicle.getSapNumber())) {
            throw new IllegalArgumentException("Vehicle with SAP number " + vehicle.getSapNumber() + " already exists");
        }
        
        // Validation: Check if chassis number already exists
        if (vehicle.getChassisNumber() != null && vehicleRepository.existsByChassisNumber(vehicle.getChassisNumber())) {
            throw new IllegalArgumentException("Vehicle with chassis number '" + vehicle.getChassisNumber() + "' already exists");
        }
        
        // Validation: Check if vehicle model exists
        if (vehicle.getVehicleModelId() != null && !vehicleModelRepository.existsById(vehicle.getVehicleModelId())) {
            throw new IllegalArgumentException("Vehicle model not found with id: " + vehicle.getVehicleModelId());
        }
        
        // Validation: Check if fuel type exists
        if (vehicle.getFuelTypeId() != null && !fuelTypeRepository.existsById(vehicle.getFuelTypeId())) {
            throw new IllegalArgumentException("Fuel type not found with id: " + vehicle.getFuelTypeId());
        }
        
        // Validation: Check if registration exists
        if (vehicle.getRegistrationId() != null && !registrationRepository.existsById(vehicle.getRegistrationId())) {
            throw new IllegalArgumentException("Registration not found with id: " + vehicle.getRegistrationId());
        }
        
        // Validation: Check if first aid kit exists
        if (vehicle.getFirstAidKitId() != null && !firstAidKitRepository.existsById(vehicle.getFirstAidKitId())) {
            throw new IllegalArgumentException("First aid kit not found with id: " + vehicle.getFirstAidKitId());
        }
        
        return vehicleRepository.save(vehicle);
    }
    
    /**
     * Update existing vehicle
     */
    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        Vehicle vehicle = vehicleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + id));
        
        // Validation: Check if new SAP number conflicts
        if (vehicleDetails.getSapNumber() != null && 
            !vehicleDetails.getSapNumber().equals(vehicle.getSapNumber()) &&
            vehicleRepository.existsBySapNumber(vehicleDetails.getSapNumber())) {
            throw new IllegalArgumentException("Vehicle with SAP number " + vehicleDetails.getSapNumber() + " already exists");
        }
        
        // Validation: Check if new chassis number conflicts
        if (vehicleDetails.getChassisNumber() != null &&
            !vehicleDetails.getChassisNumber().equals(vehicle.getChassisNumber()) &&
            vehicleRepository.existsByChassisNumber(vehicleDetails.getChassisNumber())) {
            throw new IllegalArgumentException("Vehicle with chassis number '" + vehicleDetails.getChassisNumber() + "' already exists");
        }
        
        // Validation: Check related entities exist
        if (vehicleDetails.getVehicleModelId() != null && !vehicleModelRepository.existsById(vehicleDetails.getVehicleModelId())) {
            throw new IllegalArgumentException("Vehicle model not found with id: " + vehicleDetails.getVehicleModelId());
        }
        
        if (vehicleDetails.getFuelTypeId() != null && !fuelTypeRepository.existsById(vehicleDetails.getFuelTypeId())) {
            throw new IllegalArgumentException("Fuel type not found with id: " + vehicleDetails.getFuelTypeId());
        }
        
        // Update fields
        vehicle.setSapNumber(vehicleDetails.getSapNumber());
        vehicle.setChassisNumber(vehicleDetails.getChassisNumber());
        vehicle.setEngineNumber(vehicleDetails.getEngineNumber());
        vehicle.setTagSerialNumber(vehicleDetails.getTagSerialNumber());
        vehicle.setYearOfManufacture(vehicleDetails.getYearOfManufacture());
        vehicle.setEngineDisplacement(vehicleDetails.getEngineDisplacement());
        vehicle.setPower(vehicleDetails.getPower());
        vehicle.setTireMarking(vehicleDetails.getTireMarking());
        vehicle.setFireExtinguisherSerialNumber(vehicleDetails.getFireExtinguisherSerialNumber());
        vehicle.setVehicleStatus(vehicleDetails.getVehicleStatus());
        vehicle.setStatusCode(vehicleDetails.getStatusCode());
        vehicle.setVehicleModelId(vehicleDetails.getVehicleModelId());
        vehicle.setFuelTypeId(vehicleDetails.getFuelTypeId());
        vehicle.setRegistrationId(vehicleDetails.getRegistrationId());
        vehicle.setFirstAidKitId(vehicleDetails.getFirstAidKitId());
        
        return vehicleRepository.save(vehicle);
    }
    
    /**
     * Delete vehicle by ID
     */
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehicle not found with id: " + id);
        }
        
        // TODO: Check if vehicle is assigned to any active travel orders
        
        vehicleRepository.deleteById(id);
    }
    
    /**
     * Update vehicle status
     */
    public Vehicle updateVehicleStatus(Long id, String status, Integer statusCode) {
        Vehicle vehicle = vehicleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + id));
        
        vehicle.setVehicleStatus(status);
        vehicle.setStatusCode(statusCode);
        
        return vehicleRepository.save(vehicle);
    }
    
    /**
     * Check if vehicle exists
     */
    @Transactional(readOnly = true)
    public boolean vehicleExists(Long id) {
        return vehicleRepository.existsById(id);
    }
}
