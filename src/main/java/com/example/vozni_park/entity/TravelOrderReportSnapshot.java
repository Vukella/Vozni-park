package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Denormalized snapshot of a completed travel order.
 * Populated by TravelOrderSnapshotService after each travel order is closed.
 * One row per travel order (enforced by unique constraint on travel_order_id).
 *
 * Used by Experiment C (historical snapshot benchmark) to compare reporting
 * performance against the normalized multi-JOIN query.
 */
@Entity
@Table(name = "travel_order_report_snapshot")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelOrderReportSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_snapshot")
    private Long idSnapshot;

    @Column(name = "travel_order_id", nullable = false, unique = true)
    private Long travelOrderId;

    @Column(name = "travel_order_number", nullable = false, length = 20)
    private String travelOrderNumber;

    @Column(name = "work_order_number", length = 30)
    private String workOrderNumber;

    @Column(name = "date_from")
    private LocalDate dateFrom;

    @Column(name = "date_to")
    private LocalDate dateTo;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "location_name", length = 100)
    private String locationName;

    /** Snapshot of "Brand Model (REG-NUMBER)" at the moment of closing. */
    @Column(name = "vehicle_display_name", length = 150)
    private String vehicleDisplayName;

    @Column(name = "registration_number", length = 20)
    private String registrationNumber;

    /** Comma-separated full names of all drivers on this travel order. */
    @Column(name = "driver_names", length = 500)
    private String driverNames;

    @Column(name = "starting_mileage")
    private Integer startingMileage;

    @Column(name = "ending_mileage")
    private Integer endingMileage;

    /** ending_mileage - starting_mileage */
    @Column(name = "distance_traveled")
    private Integer distanceTraveled;

    @Column(name = "total_liters", precision = 10, scale = 2)
    private BigDecimal totalLiters = BigDecimal.ZERO;

    @Column(name = "total_fuel_cost", precision = 15, scale = 2)
    private BigDecimal totalFuelCost = BigDecimal.ZERO;

    @Column(name = "snapshot_created_at", nullable = false)
    private LocalDateTime snapshotCreatedAt;
}
