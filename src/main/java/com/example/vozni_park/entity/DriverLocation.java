package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "driver_location")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_driver_location")
    private Long idDriverLocation;

    @Column(name = "driver_id", insertable = false, updatable = false)
    private Long driverId;

    @Column(name = "location_unit_id", insertable = false, updatable = false)
    private Long locationUnitId;

    // Relationships
    @OneToOne
    @JoinColumn(name = "driver_id", unique = true)
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "location_unit_id")
    private LocationUnit locationUnit;
}
