package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "vehicle_assigned_driver")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleAssignedDriver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehicle_assigned_driver")
    private Long idVehicleAssignedDriver;

    @Column(name = "vehicle_id", insertable = false, updatable = false)
    private Long vehicleId;

    @Column(name = "driver_id", insertable = false, updatable = false)
    private Long driverId;

    @Column(name = "assignment_date")
    private LocalDate assignmentDate;

    @Column(name = "note", length = 500)
    private String note;

    @OneToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;
}
