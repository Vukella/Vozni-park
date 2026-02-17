package com.example.vozni_park.controller;

import com.example.vozni_park.dto.request.LoginRequestDTO;
import com.example.vozni_park.dto.request.RefreshTokenRequestDTO;
import com.example.vozni_park.dto.response.AppUserResponseDTO;
import com.example.vozni_park.dto.response.LoginResponseDTO;
import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.security.JwtUtil;
import com.example.vozni_park.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Authentication controller for login, logout, and token refresh.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "APIs for user authentication and token management")
@Slf4j
public class AuthController {

    private final AppUserService appUserService;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * User login endpoint
     * POST /api/auth/login
     */
    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate user with username and password, returns JWT access and refresh tokens"
    )
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            log.info("Login attempt for username: {}", loginRequest.getUsername());

            // Authenticate user
            Optional<AppUser> userOptional = appUserService.authenticate(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            if (userOptional.isEmpty()) {
                log.warn("Login failed for username: {}", loginRequest.getUsername());

                // Record failed login attempt if user exists
                appUserService.getUserByUsername(loginRequest.getUsername())
                        .ifPresent(userDTO -> {
                            // We need the actual entity to record failed login
                            appUserService.recordFailedLogin(userDTO.getIdUser());
                        });

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "success", false,
                                "message", "Invalid username or password"
                        ));
            }

            // Authentication successful
            AppUser user = userOptional.get();
            log.info("Login successful for user: {} (ID: {})", user.getUsername(), user.getIdUser());

            // Generate JWT tokens
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            // Record successful login
            appUserService.recordSuccessfulLogin(user.getIdUser());

            // Get user DTO (without password)
            AppUserResponseDTO userDTO = appUserService.getUserById(user.getIdUser())
                    .orElseThrow(() -> new IllegalStateException("User not found after login"));

            // Build response
            LoginResponseDTO response = LoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpirationMs / 1000) // Convert to seconds
                    .user(userDTO)
                    .build();

            log.info("JWT tokens generated for user: {}", user.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "An error occurred during login"
                    ));
        }
    }

    /**
     * Refresh access token using refresh token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Generate a new access token using a valid refresh token"
    )
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        try {
            String refreshToken = request.getRefreshToken();

            // Validate refresh token
            if (!jwtUtil.validateToken(refreshToken)) {
                log.warn("Invalid or expired refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "success", false,
                                "message", "Invalid or expired refresh token"
                        ));
            }

            // Extract username from refresh token
            String username = jwtUtil.extractUsername(refreshToken);
            Long userId = jwtUtil.extractUserId(refreshToken);

            log.info("Refresh token request for user: {} (ID: {})", username, userId);

            // Load user from database
            Optional<AppUser> userOptional = appUserService.authenticate(username, "");
            if (userOptional.isEmpty()) {
                // User not found or inactive, try to get by ID
                AppUserResponseDTO userDTO = appUserService.getUserById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

                if (!userDTO.getIsActive()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of(
                                    "success", false,
                                    "message", "User account is inactive"
                            ));
                }
            }

            // Get full user entity for token generation
            AppUser user = appUserService.authenticate(username, "").orElse(null);
            if (user == null) {
                // Fallback: load user differently
                // This shouldn't happen in normal flow
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "success", false,
                                "message", "User not found"
                        ));
            }

            // Generate new access token
            String newAccessToken = jwtUtil.generateAccessToken(user);

            log.info("New access token generated for user: {}", username);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "accessToken", newAccessToken,
                    "tokenType", "Bearer",
                    "expiresIn", jwtExpirationMs / 1000
            ));

        } catch (Exception e) {
            log.error("Error during token refresh: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "An error occurred during token refresh"
                    ));
        }
    }

    /**
     * Logout endpoint (client-side token invalidation)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    @Operation(
            summary = "User logout",
            description = "Logout user (client should discard tokens). Server-side token blacklisting not implemented."
    )
    public ResponseEntity<?> logout() {
        // Note: In a stateless JWT system, logout is typically handled client-side
        // by discarding the tokens. For server-side invalidation, you'd need:
        // 1. Token blacklist (Redis)
        // 2. Database token storage
        // 3. Short token expiration times

        log.info("Logout request received");

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logout successful. Please discard your tokens."
        ));
    }

    /**
     * Validate token endpoint (for testing)
     * GET /api/auth/validate
     */
    @GetMapping("/validate")
    @Operation(
            summary = "Validate JWT token",
            description = "Check if a JWT token is valid (for testing purposes)"
    )
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "success", false,
                                "message", "Invalid Authorization header format. Use: Bearer <token>"
                        ));
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix

            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                String roleName = jwtUtil.extractRoleName(token);
                Long userId = jwtUtil.extractUserId(token);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "valid", true,
                        "username", username,
                        "userId", userId,
                        "roleName", roleName,
                        "message", "Token is valid"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "success", false,
                                "valid", false,
                                "message", "Token is invalid or expired"
                        ));
            }

        } catch (Exception e) {
            log.error("Error during token validation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "An error occurred during token validation"
                    ));
        }
    }
}