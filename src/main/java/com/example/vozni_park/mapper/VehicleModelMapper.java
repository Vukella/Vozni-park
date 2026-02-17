package com.example.vozni_park.mapper;

import com.example.vozni_park.dto.response.VehicleModelResponseDTO;
import com.example.vozni_park.dto.summary.BrandSummaryDTO;
import com.example.vozni_park.entity.VehicleModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VehicleModelMapper {

    public VehicleModelResponseDTO toResponseDTO(VehicleModel model) {
        if (model == null) return null;
        VehicleModelResponseDTO dto = new VehicleModelResponseDTO();
        dto.setIdVehicleModel(model.getIdVehicleModel());
        dto.setName(model.getName()); // entity.name → dto.modelName
        if (model.getBrand() != null) {
            BrandSummaryDTO brandSummary = new BrandSummaryDTO();
            brandSummary.setIdBrand(model.getBrand().getIdBrand());
            brandSummary.setName(model.getBrand().getName()); // entity.name → dto.brandName
            dto.setBrand(brandSummary);
        }
        return dto;
    }

    public List<VehicleModelResponseDTO> toResponseDTOList(List<VehicleModel> models) {
        return models.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}

