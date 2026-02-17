package com.example.vozni_park.controller;

import com.example.vozni_park.dto.response.DriversLicenseResponseDTO;
import com.example.vozni_park.entity.DriversLicense;
import com.example.vozni_park.service.DriversLicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers-licenses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DriversLicenseController {

    private final DriversLicenseService driversLicenseService;

    @GetMapping
    public ResponseEntity<List<DriversLicenseResponseDTO>> getAllDriversLicenses() {
        return ResponseEntity.ok(driversLicenseService.getAllDriversLicenses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDriversLicenseById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(driversLicenseService.getDriversLicenseById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DriversLicenseResponseDTO>> getDriversLicensesByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(driversLicenseService.getDriversLicensesByStatus(status));
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<DriversLicenseResponseDTO>> getExpiringSoon(
            @RequestParam(defaultValue = "60") int days) {
        return ResponseEntity.ok(driversLicenseService.getExpiringSoon(days));
    }

    @GetMapping("/expired")
    public ResponseEntity<List<DriversLicenseResponseDTO>> getExpired() {
        return ResponseEntity.ok(driversLicenseService.getExpired());
    }

    @PostMapping
    public ResponseEntity<?> createDriversLicense(@RequestBody DriversLicense driversLicense) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(driversLicenseService.createDriversLicense(driversLicense));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDriversLicense(@PathVariable Long id,
                                                  @RequestBody DriversLicense driversLicense) {
        try {
            return ResponseEntity.ok(driversLicenseService.updateDriversLicense(id, driversLicense));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDriversLicense(@PathVariable Long id) {
        try {
            driversLicenseService.deleteDriversLicense(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}