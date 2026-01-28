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
public class TravelOrderVehicleId implements Serializable {
    
    @Column(name = "vehicle_id")
    private Long vehicleId;
    
    @Column(name = "travel_order_id")
    private Long travelOrderId;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TravelOrderVehicleId that = (TravelOrderVehicleId) o;
        return Objects.equals(vehicleId, that.vehicleId) &&
               Objects.equals(travelOrderId, that.travelOrderId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(vehicleId, travelOrderId);
    }
}
