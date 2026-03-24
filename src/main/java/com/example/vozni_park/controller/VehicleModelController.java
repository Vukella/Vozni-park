package com.example.vozni_park.controller;

import com.example.vozni_park.dto.response.VehicleModelResponseDTO;
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
public class VehicleModelController {

    private final VehicleModelService vehicleModelService;

    @GetMapping
    public ResponseEntity<List<VehicleModelResponseDTO>> getAllVehicleModels() {
        return ResponseEntity.ok(vehicleModelService.getAllVehicleModels());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVehicleModelById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(vehicleModelService.getVehicleModelById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<List<VehicleModelResponseDTO>> getVehicleModelsByBrand(
            @PathVariable Long brandId) {
        return ResponseEntity.ok(vehicleModelService.getVehicleModelsByBrandId(brandId));
    }

    @PostMapping
    public ResponseEntity<?> createVehicleModel(@RequestBody VehicleModel vehicleModel) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(vehicleModelService.createVehicleModel(vehicleModel));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicleModel(@PathVariable Long id,
                                                @RequestBody VehicleModel vehicleModel) {
        try {
            return ResponseEntity.ok(vehicleModelService.updateVehicleModel(id, vehicleModel));
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