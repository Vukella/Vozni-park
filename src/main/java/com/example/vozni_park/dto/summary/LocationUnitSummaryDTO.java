package com.example.vozni_park.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUnitSummaryDTO {
    private Long idLocationUnit;
    private String locationName;
    private String locationAddress;
}

