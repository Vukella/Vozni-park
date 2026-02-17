package com.example.vozni_park.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuelTypeResponseDTO {
    private Long idFuelType;
    private String fuelName; // mapped from entity.name
}

