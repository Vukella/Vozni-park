package com.example.vozni_park.dto.response;

import com.example.vozni_park.dto.summary.LocationUnitSummaryDTO;
import com.example.vozni_park.dto.summary.VehicleSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleLocationResponseDTO {
    private Long idVehicleLocation;
    private Long vehicleId;
    private Long locationUnitId;
    private VehicleSummaryDTO vehicle;
    private LocationUnitSummaryDTO locationUnit;
}

