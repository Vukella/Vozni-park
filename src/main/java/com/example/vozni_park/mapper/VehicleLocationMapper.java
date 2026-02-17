package com.example.vozni_park.mapper;

import com.example.vozni_park.dto.response.VehicleLocationResponseDTO;
import com.example.vozni_park.dto.summary.LocationUnitSummaryDTO;
import com.example.vozni_park.dto.summary.VehicleSummaryDTO;
import com.example.vozni_park.entity.VehicleLocation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VehicleLocationMapper {

    public VehicleLocationResponseDTO toResponseDTO(VehicleLocation vehicleLocation) {
        if (vehicleLocation == null) return null;
        VehicleLocationResponseDTO dto = new VehicleLocationResponseDTO();
        dto.setIdVehicleLocation(vehicleLocation.getIdVehicleLocation());
        dto.setVehicleId(vehicleLocation.getVehicleId());
        dto.setLocationUnitId(vehicleLocation.getLocationUnitId());
        if (vehicleLocation.getVehicle() != null) {
            VehicleSummaryDTO vehicleSummary = new VehicleSummaryDTO();
            vehicleSummary.setIdVehicle(vehicleLocation.getVehicle().getIdVehicle());
            vehicleSummary.setSapNumber(vehicleLocation.getVehicle().getSapNumber());
            vehicleSummary.setChassisNumber(vehicleLocation.getVehicle().getChassisNumber());
            dto.setVehicle(vehicleSummary);
        }
        if (vehicleLocation.getLocationUnit() != null) {
            LocationUnitSummaryDTO locationSummary = new LocationUnitSummaryDTO();
            locationSummary.setIdLocationUnit(vehicleLocation.getLocationUnit().getIdLocationUnit());
            locationSummary.setLocationName(vehicleLocation.getLocationUnit().getLocationName());
            dto.setLocationUnit(locationSummary);
        }
        return dto;
    }

    public List<VehicleLocationResponseDTO> toResponseDTOList(List<VehicleLocation> locations) {
        return locations.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}

