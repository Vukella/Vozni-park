package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicle_subtype")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSubtype {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehicle_subtype")
    private Long idVehicleSubtype;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "vehicle_type_id", insertable = false, updatable = false)
    private Long vehicleTypeId;

    @ManyToOne
    @JoinColumn(name = "vehicle_type_id")
    private VehicleType vehicleType;
}
