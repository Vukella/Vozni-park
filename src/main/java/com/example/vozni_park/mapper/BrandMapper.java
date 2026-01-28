package com.example.vozni_park.mapper;

import com.example.vozni_park.dto.response.BrandResponseDTO;
import com.example.vozni_park.dto.summary.BrandSummaryDTO;
import com.example.vozni_park.entity.Brand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BrandMapper {

    public BrandResponseDTO toResponseDTO(Brand brand) {
        if (brand == null) {
            return null;
        }

        BrandResponseDTO dto = new BrandResponseDTO();
        dto.setIdBrand(brand.getIdBrand());
        dto.setName(brand.getName());

        // Count vehicle models if collection is loaded
        if (brand.getVehicleModels() != null) {
            dto.setVehicleModelCount(brand.getVehicleModels().size());
        } else {
            dto.setVehicleModelCount(0);
        }

        return dto;
    }

    public Brand toEntity(Brand dto) {
        if (dto == null) {
            return null;
        }

        Brand brand = new Brand();
        brand.setName(dto.getName());
        return brand;
    }

    public List<BrandResponseDTO> toResponseDTOList(List<Brand> brands) {
        if (brands == null) {
            return new ArrayList<>();
        }
        return brands.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public BrandSummaryDTO toSummaryDTO(Brand brand) {
        if (brand == null) {
            return null;
        }
        return new BrandSummaryDTO(brand.getIdBrand(), brand.getName());
    }
}

