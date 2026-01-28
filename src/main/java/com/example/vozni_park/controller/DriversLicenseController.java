package com.example.vozni_park.controller;

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
    public ResponseEntity<List<DriversLicense>> getAllDriversLicenses() {
        return ResponseEntity.ok(driversLicenseService.getAllDriversLicenses());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DriversLicense> getDriversLicenseById(@PathVariable Long id) {
        return driversLicenseService.getDriversLicenseById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DriversLicense>> getDriversLicensesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(driversLicenseService.getDriversLicensesByStatus(status));
    }
    
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<DriversLicense>> getExpiringSoon(@RequestParam(defaultValue = "60") int days) {
        return ResponseEntity.ok(driversLicenseService.getExpiringSoon(days));
    }
    
    @GetMapping("/expired")
    public ResponseEntity<List<DriversLicense>> getExpired() {
        return ResponseEntity.ok(driversLicenseService.getExpired());
    }
    
    @PostMapping
    public ResponseEntity<?> createDriversLicense(@RequestBody DriversLicense driversLicense) {
        try {
            DriversLicense created = driversLicenseService.createDriversLicense(driversLicense);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDriversLicense(@PathVariable Long id, @RequestBody DriversLicense driversLicense) {
        try {
            DriversLicense updated = driversLicenseService.updateDriversLicense(id, driversLicense);
            return ResponseEntity.ok(updated);
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
