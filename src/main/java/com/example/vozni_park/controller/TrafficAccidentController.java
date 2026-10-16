package com.example.vozni_park.controller;

import com.example.vozni_park.entity.TrafficAccident;
import com.example.vozni_park.service.TrafficAccidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traffic-accidents")
@RequiredArgsConstructor
public class TrafficAccidentController {

    private final TrafficAccidentService trafficAccidentService;

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<TrafficAccident>> getAccidentsByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(trafficAccidentService.getAccidentsByVehicle(vehicleId));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<TrafficAccident>> getAccidentsByDriver(@PathVariable Long driverId) {
        return ResponseEntity.ok(trafficAccidentService.getAccidentsByDriver(driverId));
    }

    @GetMapping("/{accidentId}")
    public ResponseEntity<TrafficAccident> getAccidentById(@PathVariable Long accidentId) {
        return ResponseEntity.ok(trafficAccidentService.getAccidentById(accidentId));
    }

    @PostMapping("/vehicle/{vehicleId}")
    public ResponseEntity<TrafficAccident> reportAccident(
            @PathVariable Long vehicleId,
            @Valid @RequestBody TrafficAccident accident,
            @AuthenticationPrincipal UserDetails userDetails) {
        TrafficAccident saved = trafficAccidentService.reportAccident(
                vehicleId, accident, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{accidentId}")
    public ResponseEntity<TrafficAccident> updateAccident(
            @PathVariable Long accidentId,
            @Valid @RequestBody TrafficAccident updatedData) {
        return ResponseEntity.ok(trafficAccidentService.updateAccident(accidentId, updatedData));
    }

    @DeleteMapping("/{accidentId}")
    public ResponseEntity<Void> deleteAccident(@PathVariable Long accidentId) {
        trafficAccidentService.deleteAccident(accidentId);
        return ResponseEntity.noContent().build();
    }
}
