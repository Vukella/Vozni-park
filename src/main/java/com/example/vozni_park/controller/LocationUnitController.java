package com.example.vozni_park.controller;

import com.example.vozni_park.dto.response.LocationUnitResponseDTO;
import com.example.vozni_park.entity.LocationUnit;
import com.example.vozni_park.service.LocationUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Location Management", description = "APIs for managing location units")
public class LocationUnitController {

    private final LocationUnitService locationUnitService;

    @GetMapping
    @Operation(summary = "Get all location units")
    public ResponseEntity<List<LocationUnitResponseDTO>> getAllLocationUnits() {
        return ResponseEntity.ok(locationUnitService.getAllLocationUnits());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get location unit by ID")
    public ResponseEntity<LocationUnitResponseDTO> getLocationUnitById(@PathVariable Long id) {
        return locationUnitService.getLocationUnitById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get location unit by name")
    public ResponseEntity<LocationUnitResponseDTO> getLocationUnitByName(@PathVariable String name) {
        return locationUnitService.getLocationUnitByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new location unit")
    public ResponseEntity<?> createLocationUnit(@RequestBody LocationUnit locationUnit) {
        try {
            LocationUnitResponseDTO created = locationUnitService.createLocationUnit(locationUnit);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update location unit")
    public ResponseEntity<?> updateLocationUnit(@PathVariable Long id, @RequestBody LocationUnit locationUnit) {
        try {
            LocationUnitResponseDTO updated = locationUnitService.updateLocationUnit(id, locationUnit);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete location unit")
    public ResponseEntity<?> deleteLocationUnit(@PathVariable Long id) {
        try {
            locationUnitService.deleteLocationUnit(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}