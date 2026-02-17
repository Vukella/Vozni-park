package com.example.vozni_park.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponseDTO {
    private Long idRegistration;
    private String registrationNumber;
    private LocalDate dateFrom;
    private LocalDate expirationDate; // mapped from entity.dateTo
    private Long policyNumber;
    private String status;
    private Integer statusCode;
    private Long vehicleId; // from vehicle back-reference (null if unassigned)
}

