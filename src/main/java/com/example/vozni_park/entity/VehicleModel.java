package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "vehicle_model")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehicle_model")
    private Long idVehicleModel;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "brand_id", nullable = false, insertable = false, updatable = false)
    private Long brandId;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @OneToMany(mappedBy = "vehicleModel")
    private List<Vehicle> vehicles;
}
