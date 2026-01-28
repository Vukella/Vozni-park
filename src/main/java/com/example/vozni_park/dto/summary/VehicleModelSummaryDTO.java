package com.example.vozni_park.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleModelSummaryDTO {
    private Long idVehicleModel;
    private String name;
    private BrandSummaryDTO brand;
}



