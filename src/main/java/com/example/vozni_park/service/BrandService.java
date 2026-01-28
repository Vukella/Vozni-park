package com.example.vozni_park.service;

import com.example.vozni_park.entity.Brand;
import com.example.vozni_park.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BrandService {
    
    private final BrandRepository brandRepository;
    
    /**
     * Get all brands
     */
    @Transactional(readOnly = true)
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }
    
    /**
     * Get brand by ID
     */
    @Transactional(readOnly = true)
    public Optional<Brand> getBrandById(Long id) {
        return brandRepository.findById(id);
    }
    
    /**
     * Get brand by name
     */
    @Transactional(readOnly = true)
    public Optional<Brand> getBrandByName(String name) {
        return brandRepository.findByName(name);
    }
    
    /**
     * Create new brand
     */
    public Brand createBrand(Brand brand) {
        // Validation: Check if brand with same name already exists
        if (brandRepository.existsByName(brand.getName())) {
            throw new IllegalArgumentException("Brand with name '" + brand.getName() + "' already exists");
        }
        
        return brandRepository.save(brand);
    }
    
    /**
     * Update existing brand
     */
    public Brand updateBrand(Long id, Brand brandDetails) {
        Brand brand = brandRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Brand not found with id: " + id));
        
        // Check if new name conflicts with existing brand
        if (!brand.getName().equals(brandDetails.getName()) && 
            brandRepository.existsByName(brandDetails.getName())) {
            throw new IllegalArgumentException("Brand with name '" + brandDetails.getName() + "' already exists");
        }
        
        brand.setName(brandDetails.getName());
        
        return brandRepository.save(brand);
    }
    
    /**
     * Delete brand by ID
     */
    public void deleteBrand(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new IllegalArgumentException("Brand not found with id: " + id);
        }
        
        // TODO: Check if brand has associated vehicle models before deleting
        // This would prevent orphaned records
        
        brandRepository.deleteById(id);
    }
    
    /**
     * Check if brand exists
     */
    @Transactional(readOnly = true)
    public boolean brandExists(Long id) {
        return brandRepository.existsById(id);
    }
}
