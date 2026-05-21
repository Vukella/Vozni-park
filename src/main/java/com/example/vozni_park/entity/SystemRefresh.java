package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Distributed lock and audit log for scheduled refresh jobs.
 *
 * One row per refresh key (e.g. 'STATUS_REFRESH').
 * The service layer acquires a pessimistic lock on this row before running
 * a scheduled job, preventing concurrent execution when multiple instances
 * of the same service are running.
 *
 * This is the application-layer equivalent of the Oracle SP_LAZY_REFRESH_PN_EXPIRE
 * SYSTEM_REFRESH table from the work version — the same concept, but owned by
 * Spring rather than the database engine.
 */
@Entity
@Table(name = "system_refresh")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemRefresh {

    @Id
    @Column(name = "key_name", length = 50)
    private String keyName;

    @Column(name = "last_run_ts")
    private LocalDateTime lastRunTs;

    /**
     * "OK" or "ERROR"
     */
    @Column(name = "last_run_status", length = 10)
    private String lastRunStatus;

    @Column(name = "last_run_message", length = 500)
    private String lastRunMessage;

    @Column(name = "last_run_by", length = 100)
    private String lastRunBy;
}
