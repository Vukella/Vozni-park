package com.example.vozni_park.entity;

import com.example.vozni_park.entity.embeddable.DriverTravelOrderId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "driver_travel_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverTravelOrder {

    @EmbeddedId
    private DriverTravelOrderId id;

    // Relationships
    @ManyToOne
    @MapsId("driverId")
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne
    @MapsId("travelOrderId")
    @JoinColumn(name = "travel_order_id", nullable = false)
    private TravelOrder travelOrder;
}
