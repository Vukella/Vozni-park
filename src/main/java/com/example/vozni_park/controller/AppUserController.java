package com.example.vozni_park.controller;

import com.example.vozni_park.dto.request.AppUserRequestDTO;
import com.example.vozni_park.dto.response.AppUserResponseDTO;
import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing application users")
public class AppUserController {

    private final AppUserService appUserService;

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<AppUserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(appUserService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<AppUserResponseDTO> getUserById(@PathVariable Long id) {
        return appUserService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username")
    public ResponseEntity<AppUserResponseDTO> getUserByUsername(@PathVariable String username) {
        return appUserService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/role/{roleId}")
    @Operation(summary = "Get users by role ID")
    public ResponseEntity<List<AppUserResponseDTO>> getUsersByRole(@PathVariable Long roleId) {
        return ResponseEntity.ok(appUserService.getUsersByRole(roleId));
    }

    @GetMapping("/role-name/{roleName}")
    @Operation(summary = "Get users by role name")
    public ResponseEntity<List<AppUserResponseDTO>> getUsersByRoleName(@PathVariable String roleName) {
        return ResponseEntity.ok(appUserService.getUsersByRoleName(roleName));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active users")
    public ResponseEntity<List<AppUserResponseDTO>> getActiveUsers() {
        return ResponseEntity.ok(appUserService.getActiveUsers());
    }

    @GetMapping("/location/{locationId}")
    @Operation(summary = "Get users by location")
    public ResponseEntity<List<AppUserResponseDTO>> getUsersByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(appUserService.getUsersByLocation(locationId));
    }

    @PostMapping
    @Operation(summary = "Create new user")
    public ResponseEntity<?> createUser(@Valid @RequestBody AppUserRequestDTO userDTO) {
        try {
            AppUserResponseDTO created = appUserService.createUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody AppUserRequestDTO userDTO) {
        try {
            AppUserResponseDTO updated = appUserService.updateUser(id, userDTO);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/password")
    @Operation(summary = "Update user password")
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
    @Operation(summary = "Update user active status")
    public ResponseEntity<?> updateUserActiveStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        try {
            AppUserResponseDTO updated = appUserService.updateUserActiveStatus(id, isActive);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with username and password")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Try to authenticate (returns entity for password check)
        var userOptional = appUserService.authenticate(username, password);

        if (userOptional.isPresent()) {
            // Success case
            AppUser user = userOptional.get();
            appUserService.recordSuccessfulLogin(user.getIdUser());

            // Return DTO (without password hash)
            AppUserResponseDTO userDTO = appUserService.getUserById(user.getIdUser()).orElseThrow();
            return ResponseEntity.ok(userDTO);
        } else {
            // Failure case - record failed login if user exists
            appUserService.getUserByUsername(username)
                    .ifPresent(userDTO -> {
                        // Get the actual user entity to record failed login
                        appUserService.authenticate(username, ""); // This will fail but we need the ID
                    });

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }
    }

    @PatchMapping("/{id}/reset-failed-logins")
    @Operation(summary = "Reset failed login attempts")
    public ResponseEntity<?> resetFailedLoginAttempts(@PathVariable Long id) {
        try {
            appUserService.resetFailedLoginAttempts(id);
            return ResponseEntity.ok().body(Map.of("message", "Failed login attempts reset"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            appUserService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}