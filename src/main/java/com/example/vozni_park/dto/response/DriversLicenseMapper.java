package com.example.vozni_park.mapper;

import com.example.vozni_park.dto.response.DriversLicenseResponseDTO;
import com.example.vozni_park.entity.DriversLicense;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DriversLicenseMapper {

    public DriversLicenseResponseDTO toResponseDTO(DriversLicense license) {
        if (license == null) return null;
        DriversLicenseResponseDTO dto = new DriversLicenseResponseDTO();
        dto.setIdDriversLicense(license.getIdDriversLicense());
        dto.setDateFrom(license.getDateFrom());
        dto.setExpirationDate(license.getDateTo()); // dateTo → expirationDate
        dto.setStatus(license.getStatus());
        dto.setStatusCode(license.getStatusCode());
        return dto;
    }

    public List<DriversLicenseResponseDTO> toResponseDTOList(List<DriversLicense> licenses) {
        return licenses.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}

