package com.example.vozni_park.controller;

import com.example.vozni_park.entity.VehicleFault;
import com.example.vozni_park.service.VehicleFaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/vehicle-faults")
@RequiredArgsConstructor
public class VehicleFaultController {

    private final VehicleFaultService vehicleFaultService;

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<VehicleFault>> getFaultsByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleFaultService.getFaultsByVehicle(vehicleId));
    }

    @GetMapping("/vehicle/{vehicleId}/active")
    public ResponseEntity<List<VehicleFault>> getActiveInoperableFaults(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleFaultService.getActiveInoperableFaults(vehicleId));
    }

    @GetMapping("/{faultId}")
    public ResponseEntity<VehicleFault> getFaultById(@PathVariable Long faultId) {
        return ResponseEntity.ok(vehicleFaultService.getFaultById(faultId));
    }

    @PostMapping("/vehicle/{vehicleId}")
    public ResponseEntity<VehicleFault> reportFault(
            @PathVariable Long vehicleId,
            @Valid @RequestBody VehicleFault fault,
            @AuthenticationPrincipal UserDetails userDetails) {
        VehicleFault saved = vehicleFaultService.reportFault(vehicleId, fault, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{faultId}/resolve")
    public ResponseEntity<VehicleFault> resolveFault(
            @PathVariable Long faultId,
            @RequestParam(required = false) LocalDate resolutionDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        VehicleFault resolved = vehicleFaultService.resolveFault(
                faultId, resolutionDate, userDetails.getUsername());
        return ResponseEntity.ok(resolved);
    }

    @PutMapping("/{faultId}")
    public ResponseEntity<VehicleFault> updateFault(
            @PathVariable Long faultId,
            @Valid @RequestBody VehicleFault updatedData) {
        return ResponseEntity.ok(vehicleFaultService.updateFault(faultId, updatedData));
    }
}
