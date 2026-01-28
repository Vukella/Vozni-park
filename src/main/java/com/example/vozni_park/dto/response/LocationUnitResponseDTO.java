package com.example.vozni_park.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUnitResponseDTO {
    private Long idLocationUnit;
    private String locationName;
    private String locationAddress;
    private Integer vehicleCount;
    private Integer driverCount;
    private Integer userCount;
}

