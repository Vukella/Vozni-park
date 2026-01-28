package com.example.vozni_park.dto.response;

import com.example.vozni_park.dto.summary.BrandSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleModelResponseDTO {
    private Long idVehicleModel;
    private String name;
    private BrandSummaryDTO brand;
    private Integer vehicleCount;
}

