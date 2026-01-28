package com.example.vozni_park.mapper;

import com.example.vozni_park.dto.response.LocationUnitResponseDTO;
import com.example.vozni_park.dto.summary.LocationUnitSummaryDTO;
import com.example.vozni_park.entity.LocationUnit;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocationUnitMapper {

    public LocationUnitResponseDTO toResponseDTO(LocationUnit location) {
        if (location == null) {
            return null;
        }

        LocationUnitResponseDTO dto = new LocationUnitResponseDTO();
        dto.setIdLocationUnit(location.getIdLocationUnit());
        dto.setLocationName(location.getLocationName());
        dto.setLocationAddress(location.getLocationAddress());

        // Count related entities if collections are loaded
        dto.setVehicleCount(location.getVehicleLocations() != null ?
                location.getVehicleLocations().size() : 0);
        dto.setDriverCount(location.getDriverLocations() != null ?
                location.getDriverLocations().size() : 0);
        dto.setUserCount(location.getUserLocations() != null ?
                location.getUserLocations().size() : 0);

        return dto;
    }

    public LocationUnit toEntity(LocationUnit dto) {
        if (dto == null) {
            return null;
        }

        LocationUnit location = new LocationUnit();
        location.setLocationName(dto.getLocationName());
        location.setLocationAddress(dto.getLocationAddress());
        return location;
    }

    public List<LocationUnitResponseDTO> toResponseDTOList(List<LocationUnit> locations) {
        if (locations == null) {
            return new ArrayList<>();
        }
        return locations.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public LocationUnitSummaryDTO toSummaryDTO(LocationUnit location) {
        if (location == null) {
            return null;
        }
        return new LocationUnitSummaryDTO(
                location.getIdLocationUnit(),
                location.getLocationName(),
                location.getLocationAddress()
        );
    }
}