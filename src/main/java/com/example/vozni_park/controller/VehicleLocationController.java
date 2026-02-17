package com.example.vozni_park.controller;

import com.example.vozni_park.dto.response.VehicleLocationResponseDTO;
import com.example.vozni_park.service.VehicleLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-locations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VehicleLocationController {

    private final VehicleLocationService vehicleLocationService;

    @GetMapping
    public ResponseEntity<List<VehicleLocationResponseDTO>> getAllVehicleLocations() {
        return ResponseEntity.ok(vehicleLocationService.getAllVehicleLocations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVehicleLocationById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(vehicleLocationService.getVehicleLocationById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<?> getVehicleLocationByVehicleId(@PathVariable Long vehicleId) {
        try {
            return ResponseEntity.ok(vehicleLocationService.getVehicleLocationByVehicleId(vehicleId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/location/{locationUnitId}")
    public ResponseEntity<List<VehicleLocationResponseDTO>> getVehicleLocationsByLocationUnitId(
            @PathVariable Long locationUnitId) {
        return ResponseEntity.ok(
                vehicleLocationService.getVehicleLocationsByLocationUnitId(locationUnitId));
    }

    @PostMapping
    public ResponseEntity<?> assignVehicleToLocation(@RequestParam Long vehicleId,
                                                     @RequestParam Long locationUnitId) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(vehicleLocationService.assignVehicleToLocation(vehicleId, locationUnitId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicleLocation(@PathVariable Long id,
                                                   @RequestParam Long locationUnitId) {
        try {
            return ResponseEntity.ok(vehicleLocationService.updateVehicleLocation(id, locationUnitId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicleLocation(@PathVariable Long id) {
        try {
            vehicleLocationService.deleteVehicleLocation(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}