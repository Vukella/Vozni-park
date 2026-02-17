package com.example.vozni_park.controller;

import com.example.vozni_park.dto.response.FirstAidKitResponseDTO;
import com.example.vozni_park.entity.FirstAidKit;
import com.example.vozni_park.service.FirstAidKitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/first-aid-kits")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FirstAidKitController {

    private final FirstAidKitService firstAidKitService;

    @GetMapping
    public ResponseEntity<List<FirstAidKitResponseDTO>> getAllFirstAidKits() {
        return ResponseEntity.ok(firstAidKitService.getAllFirstAidKits());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFirstAidKitById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(firstAidKitService.getFirstAidKitById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<FirstAidKitResponseDTO>> getFirstAidKitsByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(firstAidKitService.getFirstAidKitsByStatus(status));
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<FirstAidKitResponseDTO>> getExpiringSoon(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(firstAidKitService.getExpiringSoon(days));
    }

    @GetMapping("/expired")
    public ResponseEntity<List<FirstAidKitResponseDTO>> getExpired() {
        return ResponseEntity.ok(firstAidKitService.getExpired());
    }

    @PostMapping
    public ResponseEntity<?> createFirstAidKit(@RequestBody FirstAidKit firstAidKit) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(firstAidKitService.createFirstAidKit(firstAidKit));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFirstAidKit(@PathVariable Long id,
                                               @RequestBody FirstAidKit firstAidKit) {
        try {
            return ResponseEntity.ok(firstAidKitService.updateFirstAidKit(id, firstAidKit));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFirstAidKit(@PathVariable Long id) {
        try {
            firstAidKitService.deleteFirstAidKit(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}