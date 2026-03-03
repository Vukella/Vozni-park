package com.example.vozni_park.mapper;

import com.example.vozni_park.dto.response.FuelTypeResponseDTO;
import com.example.vozni_park.entity.FuelType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FuelTypeMapper {

    public FuelTypeResponseDTO toResponseDTO(FuelType fuelType) {
        if (fuelType == null) return null;
        FuelTypeResponseDTO dto = new FuelTypeResponseDTO();
        dto.setIdFuelType(fuelType.getIdFuelType());
        dto.setFuelName(fuelType.getName()); // entity.name → dto.fuelName
        return dto;
    }

    public List<FuelTypeResponseDTO> toResponseDTOList(List<FuelType> fuelTypes) {
        return fuelTypes.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public FuelType toEntity(String name) {
        FuelType fuelType = new FuelType();
        fuelType.setName(name);
        return fuelType;
    }
}

