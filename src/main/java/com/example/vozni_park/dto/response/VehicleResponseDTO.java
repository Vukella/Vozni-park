package com.example.vozni_park.dto.response;

import com.example.vozni_park.dto.summary.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponseDTO {
    private Long idVehicle;
    private Long sapNumber;
    private String chassisNumber;
    private Long engineNumber;
    private Long tagSerialNumber;
    private Integer yearOfManufacture;
    private BigDecimal engineDisplacement;
    private Integer power;
    private String tireMarking;
    private Long fireExtinguisherSerialNumber;
    private String vehicleStatus;
    private Integer statusCode;

    // Nested objects (using summary DTOs to avoid lazy loading)
    private RegistrationSummaryDTO registration;
    private FuelTypeSummaryDTO fuelType;
    private FirstAidKitSummaryDTO firstAidKit;
    private VehicleModelSummaryDTO vehicleModel;
    private LocationUnitSummaryDTO location;

    // Summary DTO for nested Registration
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrationSummaryDTO {
        private Long idRegistration;
        private String registrationNumber;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private String status;
    }

    // Summary DTO for nested FirstAidKit
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FirstAidKitSummaryDTO {
        private Long idFirstAidKit;
        private LocalDate expiryDate;
        private String status;
    }
}

