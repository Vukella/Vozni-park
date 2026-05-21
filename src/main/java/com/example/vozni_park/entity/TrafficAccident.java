package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "traffic_accident")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrafficAccident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_traffic_accident")
    private Long idTrafficAccident;

    @Column(name = "vehicle_id", insertable = false, updatable = false)
    private Long vehicleId;

    @Column(name = "accident_date")
    private LocalDate accidentDate;

    @Column(name = "accident_time", length = 10)
    private String accidentTime;

    @Column(name = "location_description", length = 200)
    private String locationDescription;

    @Column(name = "accident_description", length = 2000)
    private String accidentDescription;

    @Column(name = "damage_description", length = 1000)
    private String damageDescription;

    @Column(name = "reported_to_police")
    private Integer reportedToPolice = 0;

    @Column(name = "not_reported_reason", length = 500)
    private String notReportedReason;

    @Column(name = "estimated_damage_value", precision = 15, scale = 2)
    private BigDecimal estimatedDamageValue;

    @Column(name = "driver_id", insertable = false, updatable = false)
    private Long driverId;

    /** Snapshot of driver's license number at the time of the accident. */
    @Column(name = "driver_license_number_snapshot", length = 30)
    private String driverLicenseNumberSnapshot;

    /** Snapshot of driver's license expiry at the time of the accident. */
    @Column(name = "driver_license_expiry_snapshot")
    private LocalDate driverLicenseExpirySnapshot;

    @Column(name = "statement_given_by_driver_id", insertable = false, updatable = false)
    private Long statementGivenByDriverId;

    @Column(name = "location_id", insertable = false, updatable = false)
    private Long locationId;

    /** Snapshot of travel order number at the time of the accident. */
    @Column(name = "travel_order_number_snapshot", length = 30)
    private String travelOrderNumberSnapshot;

    @Column(name = "other_participants", length = 1000)
    private String otherParticipants;

    @Column(name = "declarant_national_id", length = 13)
    private String declarantNationalId;

    @Column(name = "declarant_address", length = 200)
    private String declarantAddress;

    @Column(name = "declarant_email", length = 100)
    private String declarantEmail;

    @Column(name = "declarant_position", length = 100)
    private String declarantPosition;

    @Column(name = "signature", columnDefinition = "TEXT")
    private String signature;

    @Column(name = "created_by_user_id", insertable = false, updatable = false)
    private Long createdByUserId;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statement_given_by_driver_id")
    private Driver statementGivenByDriver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private LocationUnit location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private AppUser createdByUser;
}
