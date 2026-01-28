package com.example.vozni_park.controller;

import com.example.vozni_park.entity.FuelType;
import com.example.vozni_park.service.FuelTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fuel-types")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FuelTypeController {
    
    private final FuelTypeService fuelTypeService;
    
    @GetMapping
    public ResponseEntity<List<FuelType>> getAllFuelTypes() {
        return ResponseEntity.ok(fuelTypeService.getAllFuelTypes());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<FuelType> getFuelTypeById(@PathVariable Long id) {
        return fuelTypeService.getFuelTypeById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<?> createFuelType(@RequestBody FuelType fuelType) {
        try {
            FuelType created = fuelTypeService.createFuelType(fuelType);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFuelType(@PathVariable Long id, @RequestBody FuelType fuelType) {
        try {
            FuelType updated = fuelTypeService.updateFuelType(id, fuelType);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFuelType(@PathVariable Long id) {
        try {
            fuelTypeService.deleteFuelType(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
