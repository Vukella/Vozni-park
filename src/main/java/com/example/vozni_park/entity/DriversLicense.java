package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "drivers_license")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriversLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_drivers_license")
    private Long idDriversLicense;

    @Column(name = "date_from")
    private LocalDate dateFrom;

    @Column(name = "date_to")
    private LocalDate dateTo;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "status_code")
    private Integer statusCode;

    /**
     * 1 = driver has medical restrictions that affect license validity.
     * date_to is already set to the minimum of the category-calculated expiry
     * and the medical restriction date — no special status logic needed beyond
     * the standard expiry check.
     */
    @Column(name = "medical_restrictions")
    private Integer medicalRestrictions = 0;

    @Column(name = "tachograph_card_id", insertable = false, updatable = false)
    private Long tachographCardId;

    // ── Relationships ─────────────────────────────────────────────────

    /**
     * A driving license may be associated with a tachograph card
     * (required for professional drivers of certain vehicle categories).
     */
    @OneToOne
    @JoinColumn(name = "tachograph_card_id")
    private TachographCard tachographCard;

    @OneToMany(mappedBy = "driversLicense")
    private List<DriverLicenseAssignment> driverLicenseAssignments;
}
