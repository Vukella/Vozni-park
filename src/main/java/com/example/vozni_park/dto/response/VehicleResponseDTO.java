package com.example.vozni_park.dto.response;

import com.example.vozni_park.dto.summary.FuelTypeSummaryDTO;
import com.example.vozni_park.dto.summary.LocationUnitSummaryDTO;
import com.example.vozni_park.dto.summary.VehicleModelSummaryDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class VehicleResponseDTO {

    // ── Core identity ─────────────────────────────────────────────────
    private Long idVehicle;
    private Long sapNumber;
    private String chassisNumber;
    private Long engineNumber;
    private Long tagSerialNumber;
    private Integer yearOfManufacture;
    private BigDecimal engineDisplacement;
    private Integer power;

    // ── Status ────────────────────────────────────────────────────────
    private String vehicleStatus;
    private Integer statusCode;

    // ── Physical attributes (changeset 721) ───────────────────────────
    private BigDecimal mass;
    private String color;
    private Integer loadCapacity;
    private Integer totalMass;
    private Integer numberOfSeats;
    private LocalDate firstRegistrationDate;
    private Integer operationalLeasing;
    private Integer decommissioned;
    private LocalDate decommissionDate;
    private String note;

    // ── Legacy fields (kept for backward compatibility) ───────────────
    private String tireMarking;
    private Long fireExtinguisherSerialNumber;

    // ── Related entities ──────────────────────────────────────────────
    private VehicleModelSummaryDTO vehicleModel;
    private FuelTypeSummaryDTO fuelType;
    private LocationUnitSummaryDTO location;

    private RegistrationSummaryDTO registration;
    private FirstAidKitSummaryDTO firstAidKit;
    private CascoInsuranceSummaryDTO cascoInsurance;
    private TachographDeviceSummaryDTO tachographDevice;
    private FireExtinguisherSummaryDTO fireExtinguisher;

    // ── Inner summary DTOs ────────────────────────────────────────────

    @Data
    public static class RegistrationSummaryDTO {
        private Long idRegistration;
        private String registrationNumber;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private String status;
        private Integer statusCode;
    }

    @Data
    public static class FirstAidKitSummaryDTO {
        private Long idFirstAidKit;
        private LocalDate expiryDate;
        private String status;
        private Integer statusCode;
    }

    @Data
    public static class CascoInsuranceSummaryDTO {
        private Long idCascoInsurance;
        private String contractNumber;
        private String policyNumber;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private String status;
        private Integer statusCode;
    }

    @Data
    public static class TachographDeviceSummaryDTO {
        private Long idTachographDevice;
        private String deviceType;
        private String serialNumber;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private String status;
        private Integer statusCode;
    }

    @Data
    public static class FireExtinguisherSummaryDTO {
        private Long idFireExtinguisher;
        private String serialNumber;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private String status;
        private Integer statusCode;
    }
}
