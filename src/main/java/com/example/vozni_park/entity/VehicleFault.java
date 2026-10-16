package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "vehicle_fault")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleFault {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehicle_fault")
    private Long idVehicleFault;

    @Column(name = "vehicle_id", insertable = false, updatable = false)
    private Long vehicleId;

    @Column(name = "report_date")
    private LocalDate reportDate;

    @Column(name = "mileage_at_fault")
    private Integer mileageAtFault;

    @Column(name = "fault_description", length = 1000)
    private String faultDescription;

    @Column(name = "tow_required")
    private Integer towRequired;

    /**
     * When 1, the vehicle is inoperable and its status becomes IN_FAULT (code 5).
     * Cleared (set to 0) when resolution_date is populated.
     * This is the primary flag checked by StatusCalculationService.
     */
    @Column(name = "vehicle_inoperable", nullable = false)
    private Integer vehicleInoperable = 0;

    /**
     * Service type for scheduled services: MINOR or MAJOR.
     * NULL for ordinary fault reports.
     */
    @Column(name = "service_type", length = 10)
    private String serviceType;

    /**
     * Populated when the fault is resolved. While NULL, an inoperable fault
     * keeps the vehicle in IN_FAULT status.
     */
    @Column(name = "resolution_date")
    private LocalDate resolutionDate;

    @Column(name = "handover_date")
    private LocalDate handoverDate;

    @Column(name = "signature", columnDefinition = "TEXT")
    private String signature;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_driver_id")
    private Driver reportedByDriver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handover_driver_id")
    private Driver handoverDriver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private LocationUnit location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private AppUser createdByUser;
}
