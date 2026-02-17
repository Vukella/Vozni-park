package com.example.vozni_park.mapper;

import com.example.vozni_park.dto.response.FirstAidKitResponseDTO;
import com.example.vozni_park.entity.FirstAidKit;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FirstAidKitMapper {

    public FirstAidKitResponseDTO toResponseDTO(FirstAidKit firstAidKit) {
        if (firstAidKit == null) return null;
        FirstAidKitResponseDTO dto = new FirstAidKitResponseDTO();
        dto.setIdFirstAidKit(firstAidKit.getIdFirstAidKit());
        dto.setExpirationDate(firstAidKit.getExpiryDate()); // expiryDate → expirationDate
        dto.setStatus(firstAidKit.getStatus());
        dto.setStatusCode(firstAidKit.getStatusCode());
        // vehicle back-reference - safe null check
        if (firstAidKit.getVehicle() != null) {
            dto.setVehicleId(firstAidKit.getVehicle().getIdVehicle());
        }
        return dto;
    }

    public List<FirstAidKitResponseDTO> toResponseDTOList(List<FirstAidKit> kits) {
        return kits.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}

