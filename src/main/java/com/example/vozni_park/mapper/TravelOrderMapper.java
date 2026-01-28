package com.example.vozni_park.mapper;

import com.example.vozni_park.dto.request.TravelOrderRequestDTO;
import com.example.vozni_park.dto.response.TravelOrderResponseDTO;
import com.example.vozni_park.dto.summary.*;
import com.example.vozni_park.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TravelOrderMapper {

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private DriverMapper driverMapper;

    /**
     * Convert TravelOrder entity to TravelOrderResponseDTO
     */
    public TravelOrderResponseDTO toResponseDTO(TravelOrder travelOrder) {
        if (travelOrder == null) {
            return null;
        }

        TravelOrderResponseDTO dto = new TravelOrderResponseDTO();
        dto.setIdTravelOrder(travelOrder.getIdTravelOrder());
        dto.setDateFrom(travelOrder.getDateFrom());
        dto.setDateTo(travelOrder.getDateTo());
        dto.setWorkOrderNumber(travelOrder.getWorkOrderNumber());
        dto.setTravelOrderNumber(travelOrder.getTravelOrderNumber());
        dto.setStartingMileage(travelOrder.getStartingMileage());
        dto.setEndingMileage(travelOrder.getEndingMileage());
        dto.setStatus(travelOrder.getStatus());
        dto.setCreationTime(travelOrder.getCreationTime());

        // Map Created By User (if present)
        if (travelOrder.getCreatedByUser() != null) {
            AppUser user = travelOrder.getCreatedByUser();
            AppUserSummaryDTO userDTO = new AppUserSummaryDTO();
            userDTO.setIdUser(user.getIdUser());
            userDTO.setUsername(user.getUsername());
            userDTO.setFullName(user.getFullName());

            if (user.getRole() != null) {
                Role role = user.getRole();
                userDTO.setRole(new RoleSummaryDTO(
                        role.getIdRole(),
                        role.getName()
                ));
            }
            dto.setCreatedByUser(userDTO);
        }

        // Map Location (if present)
        if (travelOrder.getLocation() != null) {
            LocationUnit location = travelOrder.getLocation();
            dto.setLocation(new LocationUnitSummaryDTO(
                    location.getIdLocationUnit(),
                    location.getLocationName(),
                    location.getLocationAddress()
            ));
        }

        // Map Drivers (if present)
        if (travelOrder.getDriverTravelOrders() != null && !travelOrder.getDriverTravelOrders().isEmpty()) {
            List<DriverSummaryDTO> drivers = travelOrder.getDriverTravelOrders().stream()
                    .map(dto1 -> driverMapper.toSummaryDTO(dto1.getDriver()))
                    .collect(Collectors.toList());
            dto.setDrivers(drivers);
        } else {
            dto.setDrivers(new ArrayList<>());
        }

        // Map Vehicles (if present)
        if (travelOrder.getTravelOrderVehicles() != null && !travelOrder.getTravelOrderVehicles().isEmpty()) {
            List<VehicleSummaryDTO> vehicles = travelOrder.getTravelOrderVehicles().stream()
                    .map(tov -> vehicleMapper.toSummaryDTO(tov.getVehicle()))
                    .collect(Collectors.toList());
            dto.setVehicles(vehicles);
        } else {
            dto.setVehicles(new ArrayList<>());
        }

        return dto;
    }

    /**
     * Convert TravelOrderRequestDTO to TravelOrder entity
     */
    public TravelOrder toEntity(TravelOrderRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        TravelOrder travelOrder = new TravelOrder();
        travelOrder.setDateFrom(dto.getDateFrom());
        travelOrder.setDateTo(dto.getDateTo());
        travelOrder.setWorkOrderNumber(dto.getWorkOrderNumber());
        travelOrder.setTravelOrderNumber(dto.getTravelOrderNumber());
        travelOrder.setStartingMileage(dto.getStartingMileage());
        travelOrder.setEndingMileage(dto.getEndingMileage());
        travelOrder.setStatus(dto.getStatus() != null ? dto.getStatus() : "IN_PROGRESS");

        // Set foreign key IDs (entities will be set by service layer)
        travelOrder.setCreatedByUserId(dto.getCreatedByUserId());
        travelOrder.setLocationId(dto.getLocationId());

        return travelOrder;
    }

    /**
     * Update existing TravelOrder entity from TravelOrderRequestDTO
     */
    public void updateEntity(TravelOrder travelOrder, TravelOrderRequestDTO dto) {
        if (travelOrder == null || dto == null) {
            return;
        }

        travelOrder.setDateFrom(dto.getDateFrom());
        travelOrder.setDateTo(dto.getDateTo());
        travelOrder.setWorkOrderNumber(dto.getWorkOrderNumber());
        travelOrder.setTravelOrderNumber(dto.getTravelOrderNumber());
        travelOrder.setStartingMileage(dto.getStartingMileage());
        travelOrder.setEndingMileage(dto.getEndingMileage());
        if (dto.getStatus() != null) {
            travelOrder.setStatus(dto.getStatus());
        }
        travelOrder.setCreatedByUserId(dto.getCreatedByUserId());
        travelOrder.setLocationId(dto.getLocationId());
    }

    /**
     * Convert list of TravelOrder entities to list of TravelOrderResponseDTOs
     */
    public List<TravelOrderResponseDTO> toResponseDTOList(List<TravelOrder> travelOrders) {
        if (travelOrders == null) {
            return new ArrayList<>();
        }
        return travelOrders.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}

