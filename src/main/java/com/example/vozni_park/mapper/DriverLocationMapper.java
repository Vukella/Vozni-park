package com.example.vozni_park.mapper;

import com.example.vozni_park.dto.response.DriverLocationResponseDTO;
import com.example.vozni_park.dto.summary.DriverSummaryDTO;
import com.example.vozni_park.dto.summary.LocationUnitSummaryDTO;
import com.example.vozni_park.entity.DriverLocation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DriverLocationMapper {

    public DriverLocationResponseDTO toResponseDTO(DriverLocation driverLocation) {
        if (driverLocation == null) return null;
        DriverLocationResponseDTO dto = new DriverLocationResponseDTO();
        dto.setIdDriverLocation(driverLocation.getIdDriverLocation());
        dto.setDriverId(driverLocation.getDriverId());
        dto.setLocationUnitId(driverLocation.getLocationUnitId());
        if (driverLocation.getDriver() != null) {
            DriverSummaryDTO driverSummary = new DriverSummaryDTO();
            driverSummary.setIdDriver(driverLocation.getDriver().getIdDriver());
            driverSummary.setSapNumber(driverLocation.getDriver().getSapNumber());
            driverSummary.setFullName(driverLocation.getDriver().getFullName());
            dto.setDriver(driverSummary);
        }
        if (driverLocation.getLocationUnit() != null) {
            LocationUnitSummaryDTO locationSummary = new LocationUnitSummaryDTO();
            locationSummary.setIdLocationUnit(driverLocation.getLocationUnit().getIdLocationUnit());
            locationSummary.setLocationName(driverLocation.getLocationUnit().getLocationName());
            dto.setLocationUnit(locationSummary);
        }
        return dto;
    }

    public List<DriverLocationResponseDTO> toResponseDTOList(List<DriverLocation> locations) {
        return locations.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}