package com.example.vozni_park.dto.response;

import com.example.vozni_park.dto.summary.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelOrderResponseDTO {
    private Long idTravelOrder;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String workOrderNumber;
    private String travelOrderNumber;
    private Long startingMileage;
    private Long endingMileage;
    private String status;
    private LocalDateTime creationTime;

    // Nested objects
    private AppUserSummaryDTO createdByUser;
    private LocationUnitSummaryDTO location;
    private List<DriverSummaryDTO> drivers;
    private List<VehicleSummaryDTO> vehicles;
}

