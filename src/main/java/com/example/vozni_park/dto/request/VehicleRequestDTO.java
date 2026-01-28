package com.example.vozni_park.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequestDTO {

    @NotNull(message = "SAP number is required")
    private Long sapNumber;

    @NotBlank(message = "Chassis number is required")
    @Size(max = 50, message = "Chassis number must not exceed 50 characters")
    private String chassisNumber;

    private Long engineNumber;
    private Long tagSerialNumber;

    @Min(value = 1900, message = "Year of manufacture must be at least 1900")
    @Max(value = 2100, message = "Year of manufacture must not exceed 2100")
    private Integer yearOfManufacture;

    @DecimalMin(value = "0.0", message = "Engine displacement must be positive")
    private BigDecimal engineDisplacement;

    @Min(value = 0, message = "Power must be positive")
    private Integer power;

    @Size(max = 50, message = "Tire marking must not exceed 50 characters")
    private String tireMarking;

    private Long fireExtinguisherSerialNumber;

    @Size(max = 20, message = "Vehicle status must not exceed 20 characters")
    private String vehicleStatus;

    private Integer statusCode;

    // Foreign key IDs
    private Long registrationId;

    @NotNull(message = "Fuel type is required")
    private Long fuelTypeId;

    private Long firstAidKitId;

    @NotNull(message = "Vehicle model is required")
    private Long vehicleModelId;
}

