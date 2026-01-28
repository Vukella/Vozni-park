package com.example.vozni_park.repository;

import com.example.vozni_park.entity.DriversLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DriversLicenseRepository extends JpaRepository<DriversLicense, Long> {
    
    // Custom query methods
    List<DriversLicense> findByStatus(String status);
    
    // Find licenses expiring soon
    @Query("SELECT dl FROM DriversLicense dl WHERE dl.dateTo BETWEEN :startDate AND :endDate")
    List<DriversLicense> findExpiringSoon(LocalDate startDate, LocalDate endDate);
    
    // Find expired licenses
    @Query("SELECT dl FROM DriversLicense dl WHERE dl.dateTo < :currentDate")
    List<DriversLicense> findExpired(LocalDate currentDate);
}
