package com.example.vozni_park.repository;

import com.example.vozni_park.entity.DriverLicenseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverLicenseAssignmentRepository extends JpaRepository<DriverLicenseAssignment, Long> {
    
    // Custom query methods
    List<DriverLicenseAssignment> findByDriverId(Long driverId);
    
    List<DriverLicenseAssignment> findByDriversLicenseId(Long driversLicenseId);
    
    List<DriverLicenseAssignment> findByLicenseCategory(String licenseCategory);
    
    Optional<DriverLicenseAssignment> findByDriverIdAndDriversLicenseId(Long driverId, Long driversLicenseId);
    
    boolean existsByDriverIdAndDriversLicenseIdAndLicenseCategory(Long driverId, Long driversLicenseId, String licenseCategory);
}
