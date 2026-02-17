package com.example.vozni_park.service;

import com.example.vozni_park.dto.response.DriversLicenseResponseDTO;
import com.example.vozni_park.entity.DriversLicense;
import com.example.vozni_park.mapper.DriversLicenseMapper;
import com.example.vozni_park.repository.DriversLicenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DriversLicenseService {

    private final DriversLicenseRepository driversLicenseRepository;
    private final DriversLicenseMapper driversLicenseMapper;

    @Transactional(readOnly = true)
    public List<DriversLicenseResponseDTO> getAllDriversLicenses() {
        return driversLicenseMapper.toResponseDTOList(driversLicenseRepository.findAll());
    }

    @Transactional(readOnly = true)
    public DriversLicenseResponseDTO getDriversLicenseById(Long id) {
        DriversLicense license = driversLicenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Drivers license not found with id: " + id));
        return driversLicenseMapper.toResponseDTO(license);
    }

    @Transactional(readOnly = true)
    public List<DriversLicenseResponseDTO> getDriversLicensesByStatus(String status) {
        return driversLicenseMapper.toResponseDTOList(driversLicenseRepository.findByStatus(status));
    }

    @Transactional(readOnly = true)
    public List<DriversLicenseResponseDTO> getExpiringSoon(int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return driversLicenseMapper.toResponseDTOList(
                driversLicenseRepository.findExpiringSoon(LocalDate.now(), cutoff) // ← changed
        );
    }

    @Transactional(readOnly = true)
    public List<DriversLicenseResponseDTO> getExpired() {
        return driversLicenseMapper.toResponseDTOList(
                driversLicenseRepository.findExpired(LocalDate.now()) // ← changed
        );
    }

    public DriversLicenseResponseDTO createDriversLicense(DriversLicense driversLicense) {
        return driversLicenseMapper.toResponseDTO(driversLicenseRepository.save(driversLicense));
    }

    public DriversLicenseResponseDTO updateDriversLicense(Long id, DriversLicense licenseDetails) {
        DriversLicense license = driversLicenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Drivers license not found with id: " + id));
        license.setDateFrom(licenseDetails.getDateFrom());
        license.setDateTo(licenseDetails.getDateTo());
        license.setStatus(licenseDetails.getStatus());
        license.setStatusCode(licenseDetails.getStatusCode());
        return driversLicenseMapper.toResponseDTO(driversLicenseRepository.save(license));
    }

    public void deleteDriversLicense(Long id) {
        if (!driversLicenseRepository.existsById(id)) {
            throw new IllegalArgumentException("Drivers license not found with id: " + id);
        }
        driversLicenseRepository.deleteById(id);
    }
}