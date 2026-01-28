package com.example.vozni_park.service;

import com.example.vozni_park.entity.DriversLicense;
import com.example.vozni_park.repository.DriversLicenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class DriversLicenseService {
    
    private final DriversLicenseRepository driversLicenseRepository;
    
    @Transactional(readOnly = true)
    public List<DriversLicense> getAllDriversLicenses() {
        return driversLicenseRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<DriversLicense> getDriversLicenseById(Long id) {
        return driversLicenseRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<DriversLicense> getDriversLicensesByStatus(String status) {
        return driversLicenseRepository.findByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public List<DriversLicense> getExpiringSoon(int days) {
        LocalDate now = LocalDate.now();
        return driversLicenseRepository.findExpiringSoon(now, now.plusDays(days));
    }
    
    @Transactional(readOnly = true)
    public List<DriversLicense> getExpired() {
        return driversLicenseRepository.findExpired(LocalDate.now());
    }
    
    public DriversLicense createDriversLicense(DriversLicense license) {
        if (license.getDateTo() != null && license.getDateFrom() != null &&
            license.getDateTo().isBefore(license.getDateFrom())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        return driversLicenseRepository.save(license);
    }
    
    public DriversLicense updateDriversLicense(Long id, DriversLicense licenseDetails) {
        DriversLicense license = driversLicenseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Drivers license not found with id: " + id));
        
        if (licenseDetails.getDateTo() != null && licenseDetails.getDateFrom() != null &&
            licenseDetails.getDateTo().isBefore(licenseDetails.getDateFrom())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        
        license.setDateFrom(licenseDetails.getDateFrom());
        license.setDateTo(licenseDetails.getDateTo());
        license.setStatus(licenseDetails.getStatus());
        license.setStatusCode(licenseDetails.getStatusCode());
        
        return driversLicenseRepository.save(license);
    }
    
    public void deleteDriversLicense(Long id) {
        if (!driversLicenseRepository.existsById(id)) {
            throw new IllegalArgumentException("Drivers license not found with id: " + id);
        }
        driversLicenseRepository.deleteById(id);
    }
}
