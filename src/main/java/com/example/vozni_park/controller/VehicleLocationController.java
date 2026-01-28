package com.example.vozni_park.controller;

import com.example.vozni_park.entity.VehicleLocation;
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
    public ResponseEntity<List<VehicleLocation>> getAllVehicleLocations() {
        return ResponseEntity.ok(vehicleLocationService.getAllVehicleLocations());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<VehicleLocation> getVehicleLocationById(@PathVariable Long id) {
        return vehicleLocationService.getVehicleLocationById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<VehicleLocation> getVehicleLocationByVehicleId(@PathVariable Long vehicleId) {
        return vehicleLocationService.getVehicleLocationByVehicleId(vehicleId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/location/{locationUnitId}")
    public ResponseEntity<List<VehicleLocation>> getVehicleLocationsByLocationUnitId(@PathVariable Long locationUnitId) {
        return ResponseEntity.ok(vehicleLocationService.getVehicleLocationsByLocationUnitId(locationUnitId));
    }
    
    @PostMapping
    public ResponseEntity<?> assignVehicleToLocation(
            @RequestParam Long vehicleId,
            @RequestParam Long locationUnitId) {
        try {
            VehicleLocation created = vehicleLocationService.assignVehicleToLocation(vehicleId, locationUnitId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicleLocation(@PathVariable Long id, @RequestParam Long locationUnitId) {
        try {
            VehicleLocation updated = vehicleLocationService.updateVehicleLocation(id, locationUnitId);
            return ResponseEntity.ok(updated);
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
