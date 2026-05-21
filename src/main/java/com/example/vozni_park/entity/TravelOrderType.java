package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "travel_order_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelOrderType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_travel_order_type")
    private Long idTravelOrderType;

    @Column(name = "name", length = 30, nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "travelOrderType")
    private List<TravelOrder> travelOrders;
}
