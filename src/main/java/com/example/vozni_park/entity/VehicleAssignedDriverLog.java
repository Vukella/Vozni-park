package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "vehicle_assigned_driver_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleAssignedDriverLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehicle_assigned_driver_log")
    private Long idVehicleAssignedDriverLog;

    @Column(name = "vehicle_id", insertable = false, updatable = false)
    private Long vehicleId;

    @Column(name = "changed_by_user_id", insertable = false, updatable = false)
    private Long changedByUserId;

    @Column(name = "change_date", nullable = false)
    private LocalDate changeDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    /**
     * The driver who held responsibility before this change.
     * NULL if this is the first assignment.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_driver_id")
    private Driver previousDriver;

    /**
     * The driver who took over responsibility.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_driver_id")
    private Driver newDriver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private AppUser changedByUser;
}
