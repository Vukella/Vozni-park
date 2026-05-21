package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "fuel_receipt")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuelReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fuel_receipt")
    private Long idFuelReceipt;

    @Column(name = "receipt_number", length = 30, nullable = false)
    private String receiptNumber;

    @Column(name = "mileage")
    private Integer mileage;

    @Column(name = "liters", precision = 10, scale = 2)
    private BigDecimal liters;

    @Column(name = "price_per_liter", precision = 10, scale = 4)
    private BigDecimal pricePerLiter;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "nis_card_id", insertable = false, updatable = false)
    private Long nisCardId;

    @ManyToOne
    @JoinColumn(name = "nis_card_id")
    private NisCard nisCard;

    @OneToMany(mappedBy = "fuelReceipt")
    private List<TravelOrderFuelReceipt> travelOrderFuelReceipts;
}
