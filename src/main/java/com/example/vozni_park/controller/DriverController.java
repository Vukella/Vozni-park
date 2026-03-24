package com.example.vozni_park.controller;

import com.example.vozni_park.dto.request.DriverRequestDTO;
import com.example.vozni_park.dto.response.DriverResponseDTO;
import com.example.vozni_park.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Tag(name = "Driver Management", description = "APIs for managing drivers")
public class DriverController {

    private final DriverService driverService;

    @GetMapping
    @Operation(summary = "Get all drivers")
    public ResponseEntity<List<DriverResponseDTO>> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @GetMapping("/available")
    @Operation(summary = "Get available drivers")
    public ResponseEntity<List<DriverResponseDTO>> getAvailableDrivers() {
        return ResponseEntity.ok(driverService.getAvailableDrivers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get driver by ID")
    public ResponseEntity<DriverResponseDTO> getDriverById(@PathVariable Long id) {
        return driverService.getDriverById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sap/{sapNumber}")
    @Operation(summary = "Get driver by SAP number")
    public ResponseEntity<DriverResponseDTO> getDriverBySapNumber(@PathVariable Long sapNumber) {
        return driverService.getDriverBySapNumber(sapNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get drivers by status")
    public ResponseEntity<List<DriverResponseDTO>> getDriversByStatus(@PathVariable String status) {
        return ResponseEntity.ok(driverService.getDriversByStatus(status));
    }

    @GetMapping("/search")
    @Operation(summary = "Search drivers by name")
    public ResponseEntity<List<DriverResponseDTO>> searchDriversByName(@RequestParam String name) {
        return ResponseEntity.ok(driverService.searchDriversByName(name));
    }

    @GetMapping("/location/{locationId}")
    @Operation(summary = "Get drivers by location")
    public ResponseEntity<List<DriverResponseDTO>> getDriversByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(driverService.getDriversByLocation(locationId));
    }

    @GetMapping("/without-location")
    @Operation(summary = "Get drivers without location")
    public ResponseEntity<List<DriverResponseDTO>> getDriversWithoutLocation() {
        return ResponseEntity.ok(driverService.getDriversWithoutLocation());
    }

    @GetMapping("/{id}/available")
    @Operation(summary = "Check if driver is available")
    public ResponseEntity<Boolean> isDriverAvailable(@PathVariable Long id) {
        boolean available = driverService.isDriverAvailable(id);
        return ResponseEntity.ok(available);
    }

    @PostMapping
    @Operation(summary = "Create new driver")
    public ResponseEntity<?> createDriver(@Valid @RequestBody DriverRequestDTO driverDTO) {
        try {
            DriverResponseDTO created = driverService.createDriver(driverDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update driver")
    public ResponseEntity<?> updateDriver(@PathVariable Long id, @Valid @RequestBody DriverRequestDTO driverDTO) {
        try {
            DriverResponseDTO updated = driverService.updateDriver(id, driverDTO);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update driver status")
    public ResponseEntity<?> updateDriverStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) Integer statusCode) {
        try {
            DriverResponseDTO updated = driverService.updateDriverStatus(id, status, statusCode);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete driver")
    public ResponseEntity<?> deleteDriver(@PathVariable Long id) {
        try {
            driverService.deleteDriver(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}