package com.example.vozni_park.repository;

import com.example.vozni_park.entity.FirstAidKit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FirstAidKitRepository extends JpaRepository<FirstAidKit, Long> {
    
    // Custom query methods
    List<FirstAidKit> findByStatus(String status);
    
    // Find kits expiring soon
    @Query("SELECT f FROM FirstAidKit f WHERE f.expiryDate BETWEEN :startDate AND :endDate")
    List<FirstAidKit> findExpiringSoon(LocalDate startDate, LocalDate endDate);
    
    // Find expired kits
    @Query("SELECT f FROM FirstAidKit f WHERE f.expiryDate < :currentDate")
    List<FirstAidKit> findExpired(LocalDate currentDate);
}
