package com.example.vozni_park.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverSummaryDTO {
    private Long idDriver;
    private Long sapNumber;
    private String fullName;
    private String phone;
    private String status;
}

