package com.example.vozni_park.service;

import com.example.vozni_park.entity.VehicleModel;
import com.example.vozni_park.repository.BrandRepository;
import com.example.vozni_park.repository.VehicleModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class VehicleModelService {
    
    private final VehicleModelRepository vehicleModelRepository;
    private final BrandRepository brandRepository;
    
    /**
     * Get all vehicle models
     */
    @Transactional(readOnly = true)
    public List<VehicleModel> getAllVehicleModels() {
        return vehicleModelRepository.findAll();
    }
    
    /**
     * Get vehicle model by ID
     */
    @Transactional(readOnly = true)
    public Optional<VehicleModel> getVehicleModelById(Long id) {
        return vehicleModelRepository.findById(id);
    }
    
    /**
     * Get vehicle models by brand ID
     */
    @Transactional(readOnly = true)
    public List<VehicleModel> getVehicleModelsByBrandId(Long brandId) {
        return vehicleModelRepository.findByBrandId(brandId);
    }
    
    /**
     * Create new vehicle model
     */
    public VehicleModel createVehicleModel(VehicleModel vehicleModel) {
        // Validation: Check if brand exists
        if (!brandRepository.existsById(vehicleModel.getBrandId())) {
            throw new IllegalArgumentException("Brand not found with id: " + vehicleModel.getBrandId());
        }
        
        // Validation: Check if model with same name already exists for this brand
        if (vehicleModelRepository.existsByNameAndBrandId(vehicleModel.getName(), vehicleModel.getBrandId())) {
            throw new IllegalArgumentException("Vehicle model with name '" + vehicleModel.getName() + 
                "' already exists for this brand");
        }
        
        return vehicleModelRepository.save(vehicleModel);
    }
    
    /**
     * Update existing vehicle model
     */
    public VehicleModel updateVehicleModel(Long id, VehicleModel modelDetails) {
        VehicleModel model = vehicleModelRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Vehicle model not found with id: " + id));
        
        // Validation: Check if brand exists
        if (!brandRepository.existsById(modelDetails.getBrandId())) {
            throw new IllegalArgumentException("Brand not found with id: " + modelDetails.getBrandId());
        }
        
        // Check if new name+brand combination conflicts
        if ((!model.getName().equals(modelDetails.getName()) || 
             !model.getBrandId().equals(modelDetails.getBrandId())) &&
            vehicleModelRepository.existsByNameAndBrandId(modelDetails.getName(), modelDetails.getBrandId())) {
            throw new IllegalArgumentException("Vehicle model with name '" + modelDetails.getName() + 
                "' already exists for this brand");
        }
        
        model.setName(modelDetails.getName());
        model.setBrandId(modelDetails.getBrandId());
        
        return vehicleModelRepository.save(model);
    }
    
    /**
     * Delete vehicle model by ID
     */
    public void deleteVehicleModel(Long id) {
        if (!vehicleModelRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehicle model not found with id: " + id);
        }
        
        // TODO: Check if model has associated vehicles before deleting
        
        vehicleModelRepository.deleteById(id);
    }
    
    /**
     * Check if vehicle model exists
     */
    @Transactional(readOnly = true)
    public boolean vehicleModelExists(Long id) {
        return vehicleModelRepository.existsById(id);
    }
}
