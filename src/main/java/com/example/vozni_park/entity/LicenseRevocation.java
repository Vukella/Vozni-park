package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "license_revocation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseRevocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_license_revocation")
    private Long idLicenseRevocation;

    @Column(name = "driver_id", insertable = false, updatable = false)
    private Long driverId;

    @Column(name = "revocation_date", nullable = false)
    private LocalDate revocationDate;

    @Column(name = "revocation_reason", length = 500, nullable = false)
    private String revocationReason;

    /**
     * NULL while revocation is still active.
     * Populated when the license is restored.
     */
    @Column(name = "restoration_date")
    private LocalDate restorationDate;

    @Column(name = "revoked_by_user_id", insertable = false, updatable = false)
    private Long revokedByUserId;

    @Column(name = "restored_by_user_id", insertable = false, updatable = false)
    private Long restoredByUserId;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revoked_by_user_id")
    private AppUser revokedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restored_by_user_id")
    private AppUser restoredByUser;
}
