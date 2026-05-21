package com.example.vozni_park.dto.response;

import com.example.vozni_park.dto.summary.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TravelOrderResponseDTO {

    private Long idTravelOrder;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String workOrderNumber;
    private String travelOrderNumber;
    private Long startingMileage;
    private Long endingMileage;

    /**
     * Human-readable status label: ACTIVE, UNFINISHED, COMPLETED, CANCELLED.
     */
    private String status;

    /**
     * Numeric status code for frontend logic: 0=CANCELLED, 1=ACTIVE, 2=UNFINISHED, 3=COMPLETED.
     */
    private Integer statusCode;

    private LocalDateTime creationTime;
    private String travelOrderTypeName;

    private AppUserSummaryDTO createdByUser;
    private LocationUnitSummaryDTO location;
    private List<DriverSummaryDTO> drivers;
    private List<VehicleSummaryDTO> vehicles;
}
