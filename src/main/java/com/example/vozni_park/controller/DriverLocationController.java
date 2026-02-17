package com.example.vozni_park.controller;

import com.example.vozni_park.dto.response.DriverLocationResponseDTO;
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
    public ResponseEntity<List<DriverLocationResponseDTO>> getAllDriverLocations() {
        return ResponseEntity.ok(driverLocationService.getAllDriverLocations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDriverLocationById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(driverLocationService.getDriverLocationById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?> getDriverLocationByDriverId(@PathVariable Long driverId) {
        try {
            return ResponseEntity.ok(driverLocationService.getDriverLocationByDriverId(driverId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/location/{locationUnitId}")
    public ResponseEntity<List<DriverLocationResponseDTO>> getDriverLocationsByLocationUnitId(
            @PathVariable Long locationUnitId) {
        return ResponseEntity.ok(
                driverLocationService.getDriverLocationsByLocationUnitId(locationUnitId));
    }

    @PostMapping
    public ResponseEntity<?> assignDriverToLocation(@RequestParam Long driverId,
                                                    @RequestParam Long locationUnitId) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(driverLocationService.assignDriverToLocation(driverId, locationUnitId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDriverLocation(@PathVariable Long id,
                                                  @RequestParam Long locationUnitId) {
        try {
            return ResponseEntity.ok(driverLocationService.updateDriverLocation(id, locationUnitId));
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