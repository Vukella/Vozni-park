package com.example.vozni_park.entity;

import com.example.vozni_park.entity.embeddable.TravelOrderVehicleId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "travel_order_vehicle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelOrderVehicle {

    @EmbeddedId
    private TravelOrderVehicleId id;

    // Relationships
    @ManyToOne
    @MapsId("vehicleId")
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne
    @MapsId("travelOrderId")
    @JoinColumn(name = "travel_order_id", nullable = false)
    private TravelOrder travelOrder;
}
