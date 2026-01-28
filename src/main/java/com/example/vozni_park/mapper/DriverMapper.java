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

    /**
     * Convert Driver entity to DriverResponseDTO
     */
    public DriverResponseDTO toResponseDTO(Driver driver) {
        if (driver == null) {
            return null;
        }

        DriverResponseDTO dto = new DriverResponseDTO();
        dto.setIdDriver(driver.getIdDriver());
        dto.setSapNumber(driver.getSapNumber());
        dto.setFullName(driver.getFullName());
        dto.setPhone(driver.getPhone());
        dto.setStatus(driver.getStatus());
        dto.setStatusCode(driver.getStatusCode());

        // Map Location (if present)
        if (driver.getDriverLocation() != null &&
                driver.getDriverLocation().getLocationUnit() != null) {
            LocationUnit location = driver.getDriverLocation().getLocationUnit();
            dto.setLocation(new LocationUnitSummaryDTO(
                    location.getIdLocationUnit(),
                    location.getLocationName(),
                    location.getLocationAddress()
            ));
        }

        // Map Driver Licenses (if present)
        if (driver.getDriverLicenseAssignments() != null && !driver.getDriverLicenseAssignments().isEmpty()) {
            List<DriverResponseDTO.DriverLicenseSummaryDTO> licenses = driver.getDriverLicenseAssignments().stream()
                    .map(assignment -> {
                        DriversLicense license = assignment.getDriversLicense();
                        return new DriverResponseDTO.DriverLicenseSummaryDTO(
                                license.getIdDriversLicense(),
                                license.getDateFrom(),
                                license.getDateTo(),
                                license.getStatus(),
                                assignment.getLicenseCategory()
                        );
                    })
                    .collect(Collectors.toList());
            dto.setLicenses(licenses);
        } else {
            dto.setLicenses(new ArrayList<>());
        }

        return dto;
    }

    /**
     * Convert DriverRequestDTO to Driver entity
     */
    public Driver toEntity(DriverRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Driver driver = new Driver();
        driver.setSapNumber(dto.getSapNumber());
        driver.setFullName(dto.getFullName());
        driver.setPhone(dto.getPhone());
        driver.setStatus(dto.getStatus());
        driver.setStatusCode(dto.getStatusCode());

        return driver;
    }

    /**
     * Update existing Driver entity from DriverRequestDTO
     */
    public void updateEntity(Driver driver, DriverRequestDTO dto) {
        if (driver == null || dto == null) {
            return;
        }

        driver.setSapNumber(dto.getSapNumber());
        driver.setFullName(dto.getFullName());
        driver.setPhone(dto.getPhone());
        driver.setStatus(dto.getStatus());
        driver.setStatusCode(dto.getStatusCode());
    }

    /**
     * Convert list of Driver entities to list of DriverResponseDTOs
     */
    public List<DriverResponseDTO> toResponseDTOList(List<Driver> drivers) {
        if (drivers == null) {
            return new ArrayList<>();
        }
        return drivers.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Driver to DriverSummaryDTO (lightweight)
     */
    public DriverSummaryDTO toSummaryDTO(Driver driver) {
        if (driver == null) {
            return null;
        }

        return new DriverSummaryDTO(
                driver.getIdDriver(),
                driver.getSapNumber(),
                driver.getFullName(),
                driver.getPhone(),
                driver.getStatus()
        );
    }
}

