package com.example.vozni_park.controller;

import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppUserController {

    private final AppUserService appUserService;

    @GetMapping
    public ResponseEntity<List<AppUser>> getAllUsers() {
        return ResponseEntity.ok(appUserService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUser> getUserById(@PathVariable Long id) {
        return appUserService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<AppUser> getUserByUsername(@PathVariable String username) {
        return appUserService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<AppUser>> getUsersByRole(@PathVariable Long roleId) {
        return ResponseEntity.ok(appUserService.getUsersByRole(roleId));
    }

    @GetMapping("/role-name/{roleName}")
    public ResponseEntity<List<AppUser>> getUsersByRoleName(@PathVariable String roleName) {
        return ResponseEntity.ok(appUserService.getUsersByRoleName(roleName));
    }

    @GetMapping("/active")
    public ResponseEntity<List<AppUser>> getActiveUsers() {
        return ResponseEntity.ok(appUserService.getActiveUsers());
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<AppUser>> getUsersByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(appUserService.getUsersByLocation(locationId));
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody AppUser user) {
        try {
            AppUser created = appUserService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody AppUser user) {
        try {
            AppUser updated = appUserService.updateUser(id, user);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("password");
            appUserService.updatePassword(id, newPassword);
            return ResponseEntity.ok().body(Map.of("message", "Password updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/active-status")
    public ResponseEntity<?> updateUserActiveStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        try {
            AppUser updated = appUserService.updateUserActiveStatus(id, isActive);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Try to authenticate
        var userOptional = appUserService.authenticate(username, password);

        if (userOptional.isPresent()) {
            // Success case
            AppUser user = userOptional.get();
            appUserService.recordSuccessfulLogin(user.getIdUser());
            return ResponseEntity.ok(user);
        } else {
            // Failure case - record failed login if user exists
            appUserService.getUserByUsername(username)
                    .ifPresent(user -> appUserService.recordFailedLogin(user.getIdUser()));

            // Return error response with proper type
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }
    }

    @PatchMapping("/{id}/reset-failed-logins")
    public ResponseEntity<?> resetFailedLoginAttempts(@PathVariable Long id) {
        try {
            appUserService.resetFailedLoginAttempts(id);
            return ResponseEntity.ok().body(Map.of("message", "Failed login attempts reset"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            appUserService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}