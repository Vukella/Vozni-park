package com.example.vozni_park.dto.response;

import com.example.vozni_park.dto.summary.DriverSummaryDTO;
import com.example.vozni_park.dto.summary.LocationUnitSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocationResponseDTO {
    private Long idDriverLocation;
    private Long driverId;
    private Long locationUnitId;
    private DriverSummaryDTO driver;
    private LocationUnitSummaryDTO locationUnit;
}

