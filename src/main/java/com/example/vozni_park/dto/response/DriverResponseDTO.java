package com.example.vozni_park.dto.response;

import com.example.vozni_park.dto.summary.LocationUnitSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponseDTO {
    private Long idDriver;
    private Long sapNumber;
    private String fullName;
    private String phone;
    private String status;
    private Integer statusCode;

    // Location information
    private LocationUnitSummaryDTO location;

    // Driver's licenses
    private List<DriverLicenseSummaryDTO> licenses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverLicenseSummaryDTO {
        private Long idDriversLicense;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private String status;
        private String licenseCategory;
    }
}

