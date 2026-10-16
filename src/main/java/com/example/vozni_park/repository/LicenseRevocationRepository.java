package com.example.vozni_park.repository;

import com.example.vozni_park.entity.LicenseRevocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRevocationRepository extends JpaRepository<LicenseRevocation, Long> {

    /**
     * Returns the currently active revocation (restoration_date IS NULL).
     * A driver can have at most one active revocation at a time.
     */
    @Query("SELECT r FROM LicenseRevocation r " +
           "WHERE r.driverId = :driverId AND r.restorationDate IS NULL")
    Optional<LicenseRevocation> findActiveRevocation(@Param("driverId") Long driverId);

    List<LicenseRevocation> findByDriverIdOrderByRevocationDateDesc(Long driverId);
}
