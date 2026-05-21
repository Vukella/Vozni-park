package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "vehicle_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehicle_type")
    private Long idVehicleType;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @OneToMany(mappedBy = "vehicleType")
    private List<VehicleSubtype> subtypes;
}
