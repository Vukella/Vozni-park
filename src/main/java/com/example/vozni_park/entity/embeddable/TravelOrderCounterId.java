package com.example.vozni_park.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelOrderCounterId implements Serializable {
    
    @Column(name = "location_id")
    private Long locationId;
    
    @Column(name = "year")
    private Integer year;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TravelOrderCounterId that = (TravelOrderCounterId) o;
        return Objects.equals(locationId, that.locationId) &&
               Objects.equals(year, that.year);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(locationId, year);
    }
}
