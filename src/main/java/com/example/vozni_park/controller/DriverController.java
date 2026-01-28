package com.example.vozni_park.controller;

import com.example.vozni_park.entity.Driver;
import com.example.vozni_park.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DriverController {
    
    private final DriverService driverService;
    
    @GetMapping
    public ResponseEntity<List<Driver>> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Driver> getDriverById(@PathVariable Long id) {
        return driverService.getDriverById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/sap/{sapNumber}")
    public ResponseEntity<Driver> getDriverBySapNumber(@PathVariable Long sapNumber) {
        return driverService.getDriverBySapNumber(sapNumber)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Driver>> getDriversByStatus(@PathVariable String status) {
        return ResponseEntity.ok(driverService.getDriversByStatus(status));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Driver>> searchDriversByName(@RequestParam String name) {
        return ResponseEntity.ok(driverService.searchDriversByName(name));
    }
    
    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<Driver>> getDriversByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(driverService.getDriversByLocation(locationId));
    }
    
    @GetMapping("/without-location")
    public ResponseEntity<List<Driver>> getDriversWithoutLocation() {
        return ResponseEntity.ok(driverService.getDriversWithoutLocation());
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<Driver>> getAvailableDrivers() {
        return ResponseEntity.ok(driverService.getAvailableDrivers());
    }
    
    @GetMapping("/{id}/available")
    public ResponseEntity<Boolean> isDriverAvailable(@PathVariable Long id) {
        boolean available = driverService.isDriverAvailable(id);
        return ResponseEntity.ok(available);
    }
    
    @PostMapping
    public ResponseEntity<?> createDriver(@RequestBody Driver driver) {
        try {
            Driver created = driverService.createDriver(driver);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDriver(@PathVariable Long id, @RequestBody Driver driver) {
        try {
            Driver updated = driverService.updateDriver(id, driver);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateDriverStatus(
            @PathVariable Long id, 
            @RequestParam String status,
            @RequestParam(required = false) Integer statusCode) {
        try {
            Driver updated = driverService.updateDriverStatus(id, status, statusCode);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDriver(@PathVariable Long id) {
        try {
            driverService.deleteDriver(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
