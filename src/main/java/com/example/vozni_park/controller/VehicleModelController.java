package com.example.vozni_park.controller;

import com.example.vozni_park.entity.VehicleModel;
import com.example.vozni_park.service.VehicleModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-models")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VehicleModelController {
    
    private final VehicleModelService vehicleModelService;
    
    @GetMapping
    public ResponseEntity<List<VehicleModel>> getAllVehicleModels() {
        return ResponseEntity.ok(vehicleModelService.getAllVehicleModels());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<VehicleModel> getVehicleModelById(@PathVariable Long id) {
        return vehicleModelService.getVehicleModelById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<List<VehicleModel>> getVehicleModelsByBrand(@PathVariable Long brandId) {
        return ResponseEntity.ok(vehicleModelService.getVehicleModelsByBrandId(brandId));
    }
    
    @PostMapping
    public ResponseEntity<?> createVehicleModel(@RequestBody VehicleModel vehicleModel) {
        try {
            VehicleModel created = vehicleModelService.createVehicleModel(vehicleModel);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicleModel(@PathVariable Long id, @RequestBody VehicleModel vehicleModel) {
        try {
            VehicleModel updated = vehicleModelService.updateVehicleModel(id, vehicleModel);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicleModel(@PathVariable Long id) {
        try {
            vehicleModelService.deleteVehicleModel(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
