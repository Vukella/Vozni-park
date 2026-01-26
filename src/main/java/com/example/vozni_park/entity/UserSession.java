package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_session",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_session_token", columnNames = "session_token"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_session")
    private Long idSession;

    @Column(name = "session_token", nullable = false, unique = true, length = 100)
    private String sessionToken;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_activity", nullable = false)
    private LocalDateTime lastActivity;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "user_id", nullable = false, insertable = false, updatable = false)
    private Long userId;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
}
