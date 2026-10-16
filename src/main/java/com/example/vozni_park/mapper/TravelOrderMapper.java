package com.example.vozni_park.mapper;

import com.example.vozni_park.dto.request.TravelOrderRequestDTO;
import com.example.vozni_park.dto.response.TravelOrderResponseDTO;
import com.example.vozni_park.dto.summary.*;
import com.example.vozni_park.entity.*;
import com.example.vozni_park.enums.TravelOrderStatus;
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

    public TravelOrderResponseDTO toResponseDTO(TravelOrder travelOrder) {
        if (travelOrder == null) return null;

        TravelOrderResponseDTO dto = new TravelOrderResponseDTO();
        dto.setIdTravelOrder(travelOrder.getIdTravelOrder());
        dto.setDateFrom(travelOrder.getDateFrom());
        dto.setDateTo(travelOrder.getDateTo());
        dto.setWorkOrderNumber(travelOrder.getWorkOrderNumber());
        dto.setTravelOrderNumber(travelOrder.getTravelOrderNumber());
        dto.setStartingMileage(travelOrder.getStartingMileage());
        dto.setEndingMileage(travelOrder.getEndingMileage());
        dto.setStatus(travelOrder.getStatus());
        dto.setStatusCode(travelOrder.getStatusCode());
        dto.setCreationTime(travelOrder.getCreationTime());

        // Travel order type name (new)
        if (travelOrder.getTravelOrderType() != null) {
            dto.setTravelOrderTypeName(travelOrder.getTravelOrderType().getName());
        }

        // Created by user
        if (travelOrder.getCreatedByUser() != null) {
            AppUser user = travelOrder.getCreatedByUser();
            AppUserSummaryDTO userDTO = new AppUserSummaryDTO();
            userDTO.setIdUser(user.getIdUser());
            userDTO.setUsername(user.getUsername());
            userDTO.setFullName(user.getFullName());
            if (user.getRole() != null) {
                userDTO.setRole(new RoleSummaryDTO(
                        user.getRole().getIdRole(),
                        user.getRole().getName()
                ));
            }
            dto.setCreatedByUser(userDTO);
        }

        // Location
        if (travelOrder.getLocation() != null) {
            LocationUnit loc = travelOrder.getLocation();
            dto.setLocation(new LocationUnitSummaryDTO(
                    loc.getIdLocationUnit(),
                    loc.getLocationName(),
                    loc.getLocationAddress()
            ));
        }

        // Drivers
        if (travelOrder.getDriverTravelOrders() != null &&
                !travelOrder.getDriverTravelOrders().isEmpty()) {
            dto.setDrivers(travelOrder.getDriverTravelOrders().stream()
                    .map(d -> driverMapper.toSummaryDTO(d.getDriver()))
                    .collect(Collectors.toList()));
        } else {
            dto.setDrivers(new ArrayList<>());
        }

        // Vehicles
        if (travelOrder.getTravelOrderVehicles() != null &&
                !travelOrder.getTravelOrderVehicles().isEmpty()) {
            dto.setVehicles(travelOrder.getTravelOrderVehicles().stream()
                    .map(tov -> vehicleMapper.toSummaryDTO(tov.getVehicle()))
                    .collect(Collectors.toList()));
        } else {
            dto.setVehicles(new ArrayList<>());
        }

        return dto;
    }

    public TravelOrder toEntity(TravelOrderRequestDTO dto) {
        if (dto == null) return null;

        TravelOrder travelOrder = new TravelOrder();
        travelOrder.setDateFrom(dto.getDateFrom());
        travelOrder.setDateTo(dto.getDateTo());
        travelOrder.setWorkOrderNumber(dto.getWorkOrderNumber());
        travelOrder.setTravelOrderNumber(dto.getTravelOrderNumber());
        travelOrder.setStartingMileage(dto.getStartingMileage());

        // Status is always ACTIVE on creation — never taken from the request.
        // StatusCalculationService owns all status transitions after this point.
        travelOrder.setStatus(TravelOrderStatus.ACTIVE.getLabel());
        travelOrder.setStatusCode(TravelOrderStatus.ACTIVE.getCode());

        return travelOrder;
    }

    /**
     * Updates only the fields a client is allowed to change on an existing order.
     * Status and statusCode are intentionally excluded — they are managed
     * exclusively by StatusCalculationService.
     */
    public void updateEntity(TravelOrder travelOrder, TravelOrderRequestDTO dto) {
        if (travelOrder == null || dto == null) return;

        travelOrder.setDateFrom(dto.getDateFrom());
        travelOrder.setDateTo(dto.getDateTo());
        travelOrder.setWorkOrderNumber(dto.getWorkOrderNumber());
        travelOrder.setStartingMileage(dto.getStartingMileage());
        // travelOrderNumber is auto-generated and immutable after creation
        // status/statusCode are owned by StatusCalculationService — never set here
    }

    public List<TravelOrderResponseDTO> toResponseDTOList(List<TravelOrder> travelOrders) {
        if (travelOrders == null) return new ArrayList<>();
        return travelOrders.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }
}
