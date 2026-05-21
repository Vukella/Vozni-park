package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Audit log for registration number changes on a vehicle.
 * change_reason values: 1=Re-registration, 2=Error correction, 3=Other
 * change_note is populated only when change_reason = 3.
 */
@Entity
@Table(name = "registration_change_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registration_change_log")
    private Long idRegistrationChangeLog;

    @Column(name = "vehicle_id", insertable = false, updatable = false)
    private Long vehicleId;

    @Column(name = "old_registration_number", length = 20)
    private String oldRegistrationNumber;

    @Column(name = "new_registration_number", length = 20, nullable = false)
    private String newRegistrationNumber;

    @Column(name = "change_reason", nullable = false)
    private Integer changeReason;

    @Column(name = "change_note", length = 300)
    private String changeNote;

    @Column(name = "change_date", nullable = false)
    private LocalDate changeDate;

    @Column(name = "changed_by_user_id", insertable = false, updatable = false)
    private Long changedByUserId;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private AppUser changedByUser;
}
