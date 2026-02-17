package com.example.vozni_park.controller;

import com.example.vozni_park.dto.response.RegistrationResponseDTO;
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
    public ResponseEntity<List<RegistrationResponseDTO>> getAllRegistrations() {
        return ResponseEntity.ok(registrationService.getAllRegistrations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRegistrationById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(registrationService.getRegistrationById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/number/{registrationNumber}")
    public ResponseEntity<?> getRegistrationByNumber(@PathVariable String registrationNumber) {
        try {
            return ResponseEntity.ok(registrationService.getRegistrationByNumber(registrationNumber));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RegistrationResponseDTO>> getRegistrationsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(registrationService.getRegistrationsByStatus(status));
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<RegistrationResponseDTO>> getExpiringSoon(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(registrationService.getExpiringSoon(days));
    }

    @GetMapping("/expired")
    public ResponseEntity<List<RegistrationResponseDTO>> getExpired() {
        return ResponseEntity.ok(registrationService.getExpired());
    }

    @PostMapping
    public ResponseEntity<?> createRegistration(@RequestBody Registration registration) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(registrationService.createRegistration(registration));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRegistration(@PathVariable Long id,
                                                @RequestBody Registration registration) {
        try {
            return ResponseEntity.ok(registrationService.updateRegistration(id, registration));
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