package com.example.vozni_park.controller;

import com.example.vozni_park.entity.VehicleAssignedDriver;
import com.example.vozni_park.entity.VehicleAssignedDriverLog;
import com.example.vozni_park.service.VehicleAssignedDriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-assigned-drivers")
@RequiredArgsConstructor
public class VehicleAssignedDriverController {

    private final VehicleAssignedDriverService vehicleAssignedDriverService;

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<VehicleAssignedDriver> getCurrentAssignment(@PathVariable Long vehicleId) {
        return vehicleAssignedDriverService.getCurrentAssignment(vehicleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/vehicle/{vehicleId}/history")
    public ResponseEntity<List<VehicleAssignedDriverLog>> getAssignmentHistory(
            @PathVariable Long vehicleId) {
        return ResponseEntity.ok(
                vehicleAssignedDriverService.getAssignmentHistory(vehicleId));
    }

    @PostMapping("/vehicle/{vehicleId}/assign/{driverId}")
    public ResponseEntity<VehicleAssignedDriver> assignDriver(
            @PathVariable Long vehicleId,
            @PathVariable Long driverId,
            @RequestParam(required = false) String note,
            @AuthenticationPrincipal UserDetails userDetails) {
        VehicleAssignedDriver assignment = vehicleAssignedDriverService
                .assignDriver(vehicleId, driverId, note, userDetails.getUsername());
        return ResponseEntity.ok(assignment);
    }

    @DeleteMapping("/vehicle/{vehicleId}")
    public ResponseEntity<Void> removeAssignment(
            @PathVariable Long vehicleId,
            @AuthenticationPrincipal UserDetails userDetails) {
        vehicleAssignedDriverService.removeAssignment(vehicleId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
