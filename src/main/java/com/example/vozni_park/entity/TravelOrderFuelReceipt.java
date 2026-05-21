package com.example.vozni_park.entity;

import com.example.vozni_park.entity.embeddable.TravelOrderFuelReceiptId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "travel_order_fuel_receipt")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelOrderFuelReceipt {

    @EmbeddedId
    private TravelOrderFuelReceiptId id;

    @ManyToOne
    @MapsId("travelOrderId")
    @JoinColumn(name = "travel_order_id")
    private TravelOrder travelOrder;

    @ManyToOne
    @MapsId("fuelReceiptId")
    @JoinColumn(name = "fuel_receipt_id")
    private FuelReceipt fuelReceipt;
}
