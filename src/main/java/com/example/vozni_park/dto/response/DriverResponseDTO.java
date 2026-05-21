package com.example.vozni_park.dto.response;

import com.example.vozni_park.dto.summary.LocationUnitSummaryDTO;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DriverResponseDTO {

    // ── Core identity ─────────────────────────────────────────────────
    private Long idDriver;
    private Long sapNumber;
    private String fullName;
    private String phone;

    // ── Status ────────────────────────────────────────────────────────
    private String status;
    private Integer statusCode;

    /**
     * 1 = license actively revoked (SUSPENDED status reason).
     * Exposed so the frontend can show the specific reason for suspension.
     */
    private Integer licenseRevoked;

    // ── Location ──────────────────────────────────────────────────────
    private LocationUnitSummaryDTO location;

    // ── Documents ─────────────────────────────────────────────────────
    private List<DriverLicenseSummaryDTO> licenses;
    private MedicalCertificateSummaryDTO medicalCertificate;
    private DriverPermitSummaryDTO driverPermit;

    // ── Inner summary DTOs ────────────────────────────────────────────

    @Data
    public static class DriverLicenseSummaryDTO {
        private Long idDriversLicense;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private String status;
        private Integer statusCode;
        private String licenseCategory;
        private Integer medicalRestrictions;
        private TachographCardSummaryDTO tachographCard;

        public DriverLicenseSummaryDTO() {}

        public DriverLicenseSummaryDTO(Long idDriversLicense, LocalDate dateFrom,
                                       LocalDate dateTo, String status, String licenseCategory) {
            this.idDriversLicense = idDriversLicense;
            this.dateFrom = dateFrom;
            this.dateTo = dateTo;
            this.status = status;
            this.licenseCategory = licenseCategory;
        }
    }

    @Data
    public static class TachographCardSummaryDTO {
        private Long idTachographCard;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private String status;
        private Integer statusCode;
    }

    @Data
    public static class MedicalCertificateSummaryDTO {
        private Long idMedicalCertificate;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private String status;
        private Integer statusCode;
    }

    @Data
    public static class DriverPermitSummaryDTO {
        private Long idDriverPermit;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private String status;
        private Integer statusCode;
    }
}
