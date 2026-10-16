package com.example.vozni_park.mapper;

import com.example.vozni_park.dto.request.DriverRequestDTO;
import com.example.vozni_park.dto.response.DriverResponseDTO;
import com.example.vozni_park.dto.summary.DriverSummaryDTO;
import com.example.vozni_park.dto.summary.LocationUnitSummaryDTO;
import com.example.vozni_park.entity.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DriverMapper {

    public DriverResponseDTO toResponseDTO(Driver driver) {
        if (driver == null) return null;

        DriverResponseDTO dto = new DriverResponseDTO();
        dto.setIdDriver(driver.getIdDriver());
        dto.setSapNumber(driver.getSapNumber());
        dto.setFullName(driver.getFullName());
        dto.setPhone(driver.getPhone());
        dto.setStatus(driver.getStatus());
        dto.setStatusCode(driver.getStatusCode());
        dto.setLicenseRevoked(driver.getLicenseRevoked());

        // Location
        if (driver.getDriverLocation() != null &&
                driver.getDriverLocation().getLocationUnit() != null) {
            LocationUnit loc = driver.getDriverLocation().getLocationUnit();
            dto.setLocation(new LocationUnitSummaryDTO(
                    loc.getIdLocationUnit(),
                    loc.getLocationName(),
                    loc.getLocationAddress()
            ));
        }

        // Medical certificate (new)
        if (driver.getMedicalCertificate() != null) {
            MedicalCertificate mc = driver.getMedicalCertificate();
            DriverResponseDTO.MedicalCertificateSummaryDTO mcDTO =
                    new DriverResponseDTO.MedicalCertificateSummaryDTO();
            mcDTO.setIdMedicalCertificate(mc.getIdMedicalCertificate());
            mcDTO.setDateFrom(mc.getDateFrom());
            mcDTO.setDateTo(mc.getDateTo());
            mcDTO.setStatus(mc.getStatus());
            mcDTO.setStatusCode(mc.getStatusCode());
            dto.setMedicalCertificate(mcDTO);
        }

        // Driver permit (new)
        if (driver.getDriverPermit() != null) {
            DriverPermit dp = driver.getDriverPermit();
            DriverResponseDTO.DriverPermitSummaryDTO dpDTO =
                    new DriverResponseDTO.DriverPermitSummaryDTO();
            dpDTO.setIdDriverPermit(dp.getIdDriverPermit());
            dpDTO.setDateFrom(dp.getDateFrom());
            dpDTO.setDateTo(dp.getDateTo());
            dpDTO.setStatus(dp.getStatus());
            dpDTO.setStatusCode(dp.getStatusCode());
            dto.setDriverPermit(dpDTO);
        }

        // Driving licenses with tachograph card (updated)
        if (driver.getDriverLicenseAssignments() != null &&
                !driver.getDriverLicenseAssignments().isEmpty()) {
            List<DriverResponseDTO.DriverLicenseSummaryDTO> licenses =
                    driver.getDriverLicenseAssignments().stream()
                            .map(assignment -> {
                                DriversLicense license = assignment.getDriversLicense();
                                DriverResponseDTO.DriverLicenseSummaryDTO licDTO =
                                        new DriverResponseDTO.DriverLicenseSummaryDTO();
                                licDTO.setIdDriversLicense(license.getIdDriversLicense());
                                licDTO.setDateFrom(license.getDateFrom());
                                licDTO.setDateTo(license.getDateTo());
                                licDTO.setStatus(license.getStatus());
                                licDTO.setStatusCode(license.getStatusCode());
                                licDTO.setLicenseCategory(assignment.getLicenseCategory());
                                licDTO.setMedicalRestrictions(license.getMedicalRestrictions());

                                // Tachograph card linked to this license
                                if (license.getTachographCard() != null) {
                                    TachographCard tc = license.getTachographCard();
                                    DriverResponseDTO.TachographCardSummaryDTO tcDTO =
                                            new DriverResponseDTO.TachographCardSummaryDTO();
                                    tcDTO.setIdTachographCard(tc.getIdTachographCard());
                                    tcDTO.setDateFrom(tc.getDateFrom());
                                    tcDTO.setDateTo(tc.getDateTo());
                                    tcDTO.setStatus(tc.getStatus());
                                    tcDTO.setStatusCode(tc.getStatusCode());
                                    licDTO.setTachographCard(tcDTO);
                                }

                                return licDTO;
                            })
                            .collect(Collectors.toList());
            dto.setLicenses(licenses);
        } else {
            dto.setLicenses(new ArrayList<>());
        }

        return dto;
    }

    public Driver toEntity(DriverRequestDTO dto) {
        if (dto == null) return null;

        Driver driver = new Driver();
        driver.setSapNumber(dto.getSapNumber());
        driver.setFullName(dto.getFullName());
        driver.setPhone(dto.getPhone());
        // Status and licenseRevoked are NOT set from request —
        // status is managed by StatusCalculationService,
        // licenseRevoked is managed by revokeDriverLicense/restoreDriverLicense.
        driver.setLicenseRevoked(0);
        return driver;
    }

    public void updateEntity(Driver driver, DriverRequestDTO dto) {
        if (driver == null || dto == null) return;

        driver.setSapNumber(dto.getSapNumber());
        driver.setFullName(dto.getFullName());
        driver.setPhone(dto.getPhone());
        // Status and licenseRevoked are intentionally NOT updated here.
    }

    public List<DriverResponseDTO> toResponseDTOList(List<Driver> drivers) {
        if (drivers == null) return new ArrayList<>();
        return drivers.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    public DriverSummaryDTO toSummaryDTO(Driver driver) {
        if (driver == null) return null;
        return new DriverSummaryDTO(
                driver.getIdDriver(),
                driver.getSapNumber(),
                driver.getFullName(),
                driver.getPhone(),
                driver.getStatus()
        );
    }
}
