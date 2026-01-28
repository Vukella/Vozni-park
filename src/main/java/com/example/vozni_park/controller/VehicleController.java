package com.example.vozni_park.controller;

import com.example.vozni_park.entity.Vehicle;
import com.example.vozni_park.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VehicleController {
    
    private final VehicleService vehicleService;
    
    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        return vehicleService.getVehicleById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/sap/{sapNumber}")
    public ResponseEntity<Vehicle> getVehicleBySapNumber(@PathVariable Long sapNumber) {
        return vehicleService.getVehicleBySapNumber(sapNumber)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/chassis/{chassisNumber}")
    public ResponseEntity<Vehicle> getVehicleByChassisNumber(@PathVariable String chassisNumber) {
        return vehicleService.getVehicleByChassisNumber(chassisNumber)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Vehicle>> getVehiclesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(vehicleService.getVehiclesByStatus(status));
    }
    
    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<Vehicle>> getVehiclesByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(vehicleService.getVehiclesByLocation(locationId));
    }
    
    @GetMapping("/without-location")
    public ResponseEntity<List<Vehicle>> getVehiclesWithoutLocation() {
        return ResponseEntity.ok(vehicleService.getVehiclesWithoutLocation());
    }
    
    @GetMapping("/model/{modelId}")
    public ResponseEntity<List<Vehicle>> getVehiclesByModel(@PathVariable Long modelId) {
        return ResponseEntity.ok(vehicleService.getVehiclesByModel(modelId));
    }
    
    @GetMapping("/fuel-type/{fuelTypeId}")
    public ResponseEntity<List<Vehicle>> getVehiclesByFuelType(@PathVariable Long fuelTypeId) {
        return ResponseEntity.ok(vehicleService.getVehiclesByFuelType(fuelTypeId));
    }
    
    @PostMapping
    public ResponseEntity<?> createVehicle(@RequestBody Vehicle vehicle) {
        try {
            Vehicle created = vehicleService.createVehicle(vehicle);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        try {
            Vehicle updated = vehicleService.updateVehicle(id, vehicle);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateVehicleStatus(
            @PathVariable Long id, 
            @RequestParam String status,
            @RequestParam(required = false) Integer statusCode) {
        try {
            Vehicle updated = vehicleService.updateVehicleStatus(id, status, statusCode);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        try {
            vehicleService.deleteVehicle(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
