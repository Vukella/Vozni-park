package com.example.vozni_park.controller;

import com.example.vozni_park.entity.Registration;
import com.example.vozni_park.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RegistrationController {
    
    private final RegistrationService registrationService;
    
    @GetMapping
    public ResponseEntity<List<Registration>> getAllRegistrations() {
        return ResponseEntity.ok(registrationService.getAllRegistrations());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Registration> getRegistrationById(@PathVariable Long id) {
        return registrationService.getRegistrationById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/number/{registrationNumber}")
    public ResponseEntity<Registration> getRegistrationByNumber(@PathVariable String registrationNumber) {
        return registrationService.getRegistrationByNumber(registrationNumber)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Registration>> getRegistrationsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(registrationService.getRegistrationsByStatus(status));
    }
    
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<Registration>> getExpiringSoon(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(registrationService.getExpiringSoon(days));
    }
    
    @GetMapping("/expired")
    public ResponseEntity<List<Registration>> getExpired() {
        return ResponseEntity.ok(registrationService.getExpired());
    }
    
    @PostMapping
    public ResponseEntity<?> createRegistration(@RequestBody Registration registration) {
        try {
            Registration created = registrationService.createRegistration(registration);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRegistration(@PathVariable Long id, @RequestBody Registration registration) {
        try {
            Registration updated = registrationService.updateRegistration(id, registration);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRegistration(@PathVariable Long id) {
        try {
            registrationService.deleteRegistration(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
