package com.example.vozni_park.controller;

import com.example.vozni_park.entity.LocationUnit;
import com.example.vozni_park.service.LocationUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LocationUnitController {
    
    private final LocationUnitService locationUnitService;
    
    @GetMapping
    public ResponseEntity<List<LocationUnit>> getAllLocationUnits() {
        return ResponseEntity.ok(locationUnitService.getAllLocationUnits());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<LocationUnit> getLocationUnitById(@PathVariable Long id) {
        return locationUnitService.getLocationUnitById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<LocationUnit> getLocationUnitByName(@PathVariable String name) {
        return locationUnitService.getLocationUnitByName(name)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<?> createLocationUnit(@RequestBody LocationUnit locationUnit) {
        try {
            LocationUnit created = locationUnitService.createLocationUnit(locationUnit);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLocationUnit(@PathVariable Long id, @RequestBody LocationUnit locationUnit) {
        try {
            LocationUnit updated = locationUnitService.updateLocationUnit(id, locationUnit);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLocationUnit(@PathVariable Long id) {
        try {
            locationUnitService.deleteLocationUnit(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
