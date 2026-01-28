package com.example.vozni_park.repository;

import com.example.vozni_park.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    
    // Custom query methods
    Optional<Registration> findByRegistrationNumber(String registrationNumber);
    
    List<Registration> findByStatus(String status);
    
    // Find registrations expiring soon
    @Query("SELECT r FROM Registration r WHERE r.dateTo BETWEEN :startDate AND :endDate")
    List<Registration> findExpiringSoon(LocalDate startDate, LocalDate endDate);
    
    // Find expired registrations
    @Query("SELECT r FROM Registration r WHERE r.dateTo < :currentDate")
    List<Registration> findExpired(LocalDate currentDate);
}
