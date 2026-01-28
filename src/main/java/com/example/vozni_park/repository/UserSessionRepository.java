package com.example.vozni_park.repository;

import com.example.vozni_park.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    // Custom query methods
    Optional<UserSession> findBySessionToken(String sessionToken);
    
    List<UserSession> findByUserId(Long userId);
    
    List<UserSession> findByUserIdAndIsActive(Long userId, Boolean isActive);
    
    // Find active sessions for a user
    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.isActive = true AND s.expiresAt > :currentTime")
    List<UserSession> findActiveSessionsByUserId(Long userId, LocalDateTime currentTime);
    
    // Find expired sessions
    @Query("SELECT s FROM UserSession s WHERE s.expiresAt < :currentTime AND s.isActive = true")
    List<UserSession> findExpiredSessions(LocalDateTime currentTime);
    
    // Find sessions to clean up (inactive or expired)
    @Query("SELECT s FROM UserSession s WHERE s.isActive = false OR s.expiresAt < :currentTime")
    List<UserSession> findSessionsToCleanup(LocalDateTime currentTime);
    
    boolean existsBySessionToken(String sessionToken);
}
