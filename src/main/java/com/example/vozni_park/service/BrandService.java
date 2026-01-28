package com.example.vozni_park.service;

import com.example.vozni_park.dto.response.BrandResponseDTO;
import com.example.vozni_park.entity.Brand;
import com.example.vozni_park.mapper.BrandMapper;
import com.example.vozni_park.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    /**
     * Get all brands - returns DTOs
     */
    public List<BrandResponseDTO> getAllBrands() {
        List<Brand> brands = brandRepository.findAll();
        return brandMapper.toResponseDTOList(brands);
    }

    /**
     * Get brand by ID
     */
    public Optional<BrandResponseDTO> getBrandById(Long id) {
        return brandRepository.findById(id)
                .map(brandMapper::toResponseDTO);
    }

    /**
     * Get brand by name
     */
    public Optional<BrandResponseDTO> getBrandByName(String name) {
        return brandRepository.findByName(name)
                .map(brandMapper::toResponseDTO);
    }

    /**
     * Create new brand
     */
    @Transactional
    public BrandResponseDTO createBrand(Brand brand) {
        // Validation: Check if brand with same name already exists
        if (brandRepository.existsByName(brand.getName())) {
            throw new IllegalArgumentException("Brand with name '" + brand.getName() + "' already exists");
        }

        Brand saved = brandRepository.save(brand);
        return brandMapper.toResponseDTO(saved);
    }

    /**
     * Update existing brand
     */
    @Transactional
    public BrandResponseDTO updateBrand(Long id, Brand brandDetails) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found with id: " + id));

        // Check if new name conflicts with existing brand
        if (!brand.getName().equals(brandDetails.getName()) &&
                brandRepository.existsByName(brandDetails.getName())) {
            throw new IllegalArgumentException("Brand with name '" + brandDetails.getName() + "' already exists");
        }

        brand.setName(brandDetails.getName());

        Brand updated = brandRepository.save(brand);
        return brandMapper.toResponseDTO(updated);
    }

    /**
     * Delete brand by ID
     */
    @Transactional
    public void deleteBrand(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new IllegalArgumentException("Brand not found with id: " + id);
        }
        brandRepository.deleteById(id);
    }

    /**
     * Check if brand exists
     */
    public boolean brandExists(Long id) {
        return brandRepository.existsById(id);
    }
}