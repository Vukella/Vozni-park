package com.example.vozni_park.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelOrderFuelReceiptId implements Serializable {

    @Column(name = "travel_order_id")
    private Long travelOrderId;

    @Column(name = "fuel_receipt_id")
    private Long fuelReceiptId;
}
