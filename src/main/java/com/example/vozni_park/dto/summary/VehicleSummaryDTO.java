package com.example.vozni_park.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSummaryDTO {
    private Long idVehicle;
    private Long sapNumber;
    private String chassisNumber;
    private String vehicleStatus;
    private VehicleModelSummaryDTO vehicleModel;
}

