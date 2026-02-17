package com.example.vozni_park.mapper;

import com.example.vozni_park.dto.response.RegistrationResponseDTO;
import com.example.vozni_park.entity.Registration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RegistrationMapper {

    public RegistrationResponseDTO toResponseDTO(Registration registration) {
        if (registration == null) return null;
        RegistrationResponseDTO dto = new RegistrationResponseDTO();
        dto.setIdRegistration(registration.getIdRegistration());
        dto.setRegistrationNumber(registration.getRegistrationNumber());
        dto.setDateFrom(registration.getDateFrom());
        dto.setExpirationDate(registration.getDateTo()); // dateTo → expirationDate
        dto.setPolicyNumber(registration.getPolicyNumber());
        dto.setStatus(registration.getStatus());
        dto.setStatusCode(registration.getStatusCode());
        // vehicle back-reference - safe null check
        if (registration.getVehicle() != null) {
            dto.setVehicleId(registration.getVehicle().getIdVehicle());
        }
        return dto;
    }

    public List<RegistrationResponseDTO> toResponseDTOList(List<Registration> registrations) {
        return registrations.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}

