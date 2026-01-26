package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "fuel_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuelType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fuel_type")
    private Long idFuelType;

    @Column(name = "name", length = 30)
    private String name;

    // Relationships
    @OneToMany(mappedBy = "fuelType")
    private List<Vehicle> vehicles;
}
