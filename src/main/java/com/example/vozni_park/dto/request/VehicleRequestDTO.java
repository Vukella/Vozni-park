package com.example.vozni_park.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class VehicleRequestDTO {

    // ── Required ──────────────────────────────────────────────────────
    @NotNull(message = "Vehicle model is required")
    private Long vehicleModelId;

    @NotNull(message = "Fuel type is required")
    private Long fuelTypeId;

    // ── Optional core fields ──────────────────────────────────────────
    private Long sapNumber;

    @Size(max = 50, message = "Chassis number must not exceed 50 characters")
    private String chassisNumber;

    private Long engineNumber;
    private Long tagSerialNumber;

    @Positive(message = "Year of manufacture must be positive")
    private Integer yearOfManufacture;

    private BigDecimal engineDisplacement;

    @Positive(message = "Power must be positive")
    private Integer power;

    // ── Physical attributes (from changeset 721) ──────────────────────
    private BigDecimal mass;

    @Size(max = 20, message = "Color must not exceed 20 characters")
    private String color;

    private Integer loadCapacity;
    private Integer totalMass;
    private Integer numberOfSeats;
    private LocalDate firstRegistrationDate;
    private Integer operationalLeasing;

    @Size(max = 300, message = "Note must not exceed 300 characters")
    private String note;

    // ── Legacy fields ─────────────────────────────────────────────────
    @Size(max = 50, message = "Tire marking must not exceed 50 characters")
    private String tireMarking;

    private Long fireExtinguisherSerialNumber;

    // ── Status (read-only from client perspective — managed by StatusCalculationService)
    // Accepted in request for backward compatibility but ignored on write;
    // status is always recalculated after save.
    private String vehicleStatus;
    private Integer statusCode;

    // ── Related entity IDs ────────────────────────────────────────────
    private Long registrationId;
    private Long firstAidKitId;
    private Long cascoInsuranceId;
    private Long tachographDeviceId;
    private Long fireExtinguisherId;
    private Long nisCardId;
    private Long vehicleTypeId;
    private Long vehicleSubtypeId;
}
