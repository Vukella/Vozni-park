package com.example.vozni_park.controller;

import com.example.vozni_park.dto.response.FuelTypeResponseDTO;
import com.example.vozni_park.service.FuelTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fuel-types")
@RequiredArgsConstructor
public class FuelTypeController {

    private final FuelTypeService fuelTypeService;

    @GetMapping
    public ResponseEntity<List<FuelTypeResponseDTO>> getAllFuelTypes() {
        return ResponseEntity.ok(fuelTypeService.getAllFuelTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuelTypeResponseDTO> getFuelTypeById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(fuelTypeService.getFuelTypeById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createFuelType(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(fuelTypeService.createFuelType(body.get("name")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFuelType(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(fuelTypeService.updateFuelType(id, body.get("name")));
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