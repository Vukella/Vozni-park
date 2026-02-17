package com.example.vozni_park.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriversLicenseResponseDTO {
    private Long idDriversLicense;
    private LocalDate dateFrom;
    private LocalDate expirationDate; // mapped from entity.dateTo
    private String status;
    private Integer statusCode;
}