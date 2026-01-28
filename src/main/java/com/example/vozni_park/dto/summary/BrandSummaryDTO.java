package com.example.vozni_park.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandSummaryDTO {
    private Long idBrand;
    private String name;
}