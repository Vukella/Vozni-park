package com.example.vozni_park.controller;

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
    public ResponseEntity<List<FirstAidKit>> getAllFirstAidKits() {
        return ResponseEntity.ok(firstAidKitService.getAllFirstAidKits());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<FirstAidKit> getFirstAidKitById(@PathVariable Long id) {
        return firstAidKitService.getFirstAidKitById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<FirstAidKit>> getFirstAidKitsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(firstAidKitService.getFirstAidKitsByStatus(status));
    }
    
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<FirstAidKit>> getExpiringSoon(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(firstAidKitService.getExpiringSoon(days));
    }
    
    @GetMapping("/expired")
    public ResponseEntity<List<FirstAidKit>> getExpired() {
        return ResponseEntity.ok(firstAidKitService.getExpired());
    }
    
    @PostMapping
    public ResponseEntity<?> createFirstAidKit(@RequestBody FirstAidKit firstAidKit) {
        try {
            FirstAidKit created = firstAidKitService.createFirstAidKit(firstAidKit);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFirstAidKit(@PathVariable Long id, @RequestBody FirstAidKit firstAidKit) {
        try {
            FirstAidKit updated = firstAidKitService.updateFirstAidKit(id, firstAidKit);
            return ResponseEntity.ok(updated);
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
