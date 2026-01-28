package com.example.vozni_park.controller;

import com.example.vozni_park.dto.request.VehicleRequestDTO;
import com.example.vozni_park.dto.response.VehicleResponseDTO;
import com.example.vozni_park.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Vehicle Management", description = "APIs for managing vehicles in the fleet")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    @Operation(summary = "Get all vehicles", description = "Retrieve a list of all vehicles in the fleet")
    public ResponseEntity<List<VehicleResponseDTO>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID", description = "Retrieve a specific vehicle by its ID")
    public ResponseEntity<VehicleResponseDTO> getVehicleById(@PathVariable Long id) {
        return vehicleService.getVehicleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sap/{sapNumber}")
    @Operation(summary = "Get vehicle by SAP number")
    public ResponseEntity<VehicleResponseDTO> getVehicleBySapNumber(@PathVariable Long sapNumber) {
        return vehicleService.getVehicleBySapNumber(sapNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/chassis/{chassisNumber}")
    @Operation(summary = "Get vehicle by chassis number")
    public ResponseEntity<VehicleResponseDTO> getVehicleByChassisNumber(@PathVariable String chassisNumber) {
        return vehicleService.getVehicleByChassisNumber(chassisNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get vehicles by status")
    public ResponseEntity<List<VehicleResponseDTO>> getVehiclesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(vehicleService.getVehiclesByStatus(status));
    }

    @GetMapping("/location/{locationId}")
    @Operation(summary = "Get vehicles by location")
    public ResponseEntity<List<VehicleResponseDTO>> getVehiclesByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(vehicleService.getVehiclesByLocation(locationId));
    }

    @GetMapping("/without-location")
    @Operation(summary = "Get vehicles without location")
    public ResponseEntity<List<VehicleResponseDTO>> getVehiclesWithoutLocation() {
        return ResponseEntity.ok(vehicleService.getVehiclesWithoutLocation());
    }

    @GetMapping("/model/{modelId}")
    @Operation(summary = "Get vehicles by model")
    public ResponseEntity<List<VehicleResponseDTO>> getVehiclesByModel(@PathVariable Long modelId) {
        return ResponseEntity.ok(vehicleService.getVehiclesByModel(modelId));
    }

    @GetMapping("/fuel-type/{fuelTypeId}")
    @Operation(summary = "Get vehicles by fuel type")
    public ResponseEntity<List<VehicleResponseDTO>> getVehiclesByFuelType(@PathVariable Long fuelTypeId) {
        return ResponseEntity.ok(vehicleService.getVehiclesByFuelType(fuelTypeId));
    }

    @PostMapping
    @Operation(summary = "Create new vehicle", description = "Add a new vehicle to the fleet")
    public ResponseEntity<?> createVehicle(@Valid @RequestBody VehicleRequestDTO vehicleDTO) {
        try {
            VehicleResponseDTO created = vehicleService.createVehicle(vehicleDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle", description = "Update an existing vehicle's information")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id, @Valid @RequestBody VehicleRequestDTO vehicleDTO) {
        try {
            VehicleResponseDTO updated = vehicleService.updateVehicle(id, vehicleDTO);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update vehicle status")
    public ResponseEntity<?> updateVehicleStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) Integer statusCode) {
        try {
            VehicleResponseDTO updated = vehicleService.updateVehicleStatus(id, status, statusCode);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        try {
            vehicleService.deleteVehicle(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}