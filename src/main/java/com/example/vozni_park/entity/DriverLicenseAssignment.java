package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "driver_license_assignment",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_driver_license_assignment",
                columnNames = {"driver_id", "drivers_license_id", "license_category"}
        ))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverLicenseAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_driver_license_assignment")
    private Long idDriverLicenseAssignment;

    @Column(name = "driver_id", nullable = false, insertable = false, updatable = false)
    private Long driverId;

    @Column(name = "drivers_license_id", nullable = false, insertable = false, updatable = false)
    private Long driversLicenseId;

    @Column(name = "license_category", length = 20)
    private String licenseCategory;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "drivers_license_id", nullable = false)
    private DriversLicense driversLicense;
}
