package com.example.vozni_park.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelOrderRequestDTO {

    @NotNull(message = "Start date is required")
    private LocalDate dateFrom;

    @NotNull(message = "End date is required")
    private LocalDate dateTo;

    @NotBlank(message = "Work order number is required")
    @Size(max = 30, message = "Work order number must not exceed 30 characters")
    private String workOrderNumber;

    @Size(max = 20, message = "Travel order number must not exceed 20 characters")
    private String travelOrderNumber;

    @NotNull(message = "Starting mileage is required")
    @Min(value = 0, message = "Starting mileage must be positive")
    private Long startingMileage;

    @Min(value = 0, message = "Ending mileage must be positive")
    private Long endingMileage;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    @NotNull(message = "Created by user ID is required")
    private Long createdByUserId;

    @NotNull(message = "Location ID is required")
    private Long locationId;

    // Lists of IDs for many-to-many relationships
    private List<Long> driverIds;
    private List<Long> vehicleIds;
}

