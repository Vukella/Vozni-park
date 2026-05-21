package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_password_reset_log")
    private Long idPasswordResetLog;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "reset_time", nullable = false)
    private LocalDateTime resetTime;

    @Column(name = "reset_by_user_id", insertable = false, updatable = false)
    private Long resetByUserId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "notes", length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reset_by_user_id")
    private AppUser resetByUser;
}
