package com.example.vozni_park.controller;

import com.example.vozni_park.entity.DriverLocation;
import com.example.vozni_park.service.DriverLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/driver-locations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DriverLocationController {
    
    private final DriverLocationService driverLocationService;
    
    @GetMapping
    public ResponseEntity<List<DriverLocation>> getAllDriverLocations() {
        return ResponseEntity.ok(driverLocationService.getAllDriverLocations());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DriverLocation> getDriverLocationById(@PathVariable Long id) {
        return driverLocationService.getDriverLocationById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<DriverLocation> getDriverLocationByDriverId(@PathVariable Long driverId) {
        return driverLocationService.getDriverLocationByDriverId(driverId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/location/{locationUnitId}")
    public ResponseEntity<List<DriverLocation>> getDriverLocationsByLocationUnitId(@PathVariable Long locationUnitId) {
        return ResponseEntity.ok(driverLocationService.getDriverLocationsByLocationUnitId(locationUnitId));
    }
    
    @PostMapping
    public ResponseEntity<?> assignDriverToLocation(
            @RequestParam Long driverId,
            @RequestParam Long locationUnitId) {
        try {
            DriverLocation created = driverLocationService.assignDriverToLocation(driverId, locationUnitId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDriverLocation(@PathVariable Long id, @RequestParam Long locationUnitId) {
        try {
            DriverLocation updated = driverLocationService.updateDriverLocation(id, locationUnitId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDriverLocation(@PathVariable Long id) {
        try {
            driverLocationService.deleteDriverLocation(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
