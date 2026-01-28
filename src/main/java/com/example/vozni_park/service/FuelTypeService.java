package com.example.vozni_park.service;

import com.example.vozni_park.entity.FuelType;
import com.example.vozni_park.repository.FuelTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class FuelTypeService {
    
    private final FuelTypeRepository fuelTypeRepository;
    
    /**
     * Get all fuel types
     */
    @Transactional(readOnly = true)
    public List<FuelType> getAllFuelTypes() {
        return fuelTypeRepository.findAll();
    }
    
    /**
     * Get fuel type by ID
     */
    @Transactional(readOnly = true)
    public Optional<FuelType> getFuelTypeById(Long id) {
        return fuelTypeRepository.findById(id);
    }
    
    /**
     * Get fuel type by name
     */
    @Transactional(readOnly = true)
    public Optional<FuelType> getFuelTypeByName(String name) {
        return fuelTypeRepository.findByName(name);
    }
    
    /**
     * Create new fuel type
     */
    public FuelType createFuelType(FuelType fuelType) {
        // Validation: Check if fuel type with same name already exists
        if (fuelTypeRepository.existsByName(fuelType.getName())) {
            throw new IllegalArgumentException("Fuel type with name '" + fuelType.getName() + "' already exists");
        }
        
        return fuelTypeRepository.save(fuelType);
    }
    
    /**
     * Update existing fuel type
     */
    public FuelType updateFuelType(Long id, FuelType fuelTypeDetails) {
        FuelType fuelType = fuelTypeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Fuel type not found with id: " + id));
        
        // Check if new name conflicts
        if (!fuelType.getName().equals(fuelTypeDetails.getName()) && 
            fuelTypeRepository.existsByName(fuelTypeDetails.getName())) {
            throw new IllegalArgumentException("Fuel type with name '" + fuelTypeDetails.getName() + "' already exists");
        }
        
        fuelType.setName(fuelTypeDetails.getName());
        
        return fuelTypeRepository.save(fuelType);
    }
    
    /**
     * Delete fuel type by ID
     */
    public void deleteFuelType(Long id) {
        if (!fuelTypeRepository.existsById(id)) {
            throw new IllegalArgumentException("Fuel type not found with id: " + id);
        }
        
        fuelTypeRepository.deleteById(id);
    }
    
    /**
     * Check if fuel type exists
     */
    @Transactional(readOnly = true)
    public boolean fuelTypeExists(Long id) {
        return fuelTypeRepository.existsById(id);
    }
}
