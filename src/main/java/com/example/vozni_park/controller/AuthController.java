package com.example.vozni_park.controller;

import com.example.vozni_park.dto.request.LoginRequestDTO;
import com.example.vozni_park.dto.request.OtpVerifyRequestDTO;
import com.example.vozni_park.dto.request.RefreshTokenRequestDTO;
import com.example.vozni_park.dto.response.AppUserResponseDTO;
import com.example.vozni_park.dto.response.LoginResponseDTO;
import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.repository.AppUserRepository;
import com.example.vozni_park.security.JwtUtil;
import com.example.vozni_park.service.AppUserService;
import com.example.vozni_park.service.TwoFactorAuthService;
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
    private final TwoFactorAuthService twoFactorAuthService;
    private final AppUserRepository appUserRepository;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * User login endpoint
     * POST /api/auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "User login - step 1", description = "Validates credentials, issues JWT directly if skip flag set, otherwise sends OTP")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            log.info("Login attempt for username: {}", loginRequest.getUsername());

            Optional<AppUser> userOptional = appUserService.authenticate(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            if (userOptional.isEmpty()) {
                log.warn("Login failed for username: {}", loginRequest.getUsername());
                appUserService.getUserByUsername(loginRequest.getUsername())
                        .ifPresent(userDTO -> appUserService.recordFailedLogin(userDTO.getIdUser()));

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Invalid username or password"));
            }

            AppUser user = userOptional.get();
            appUserService.recordSuccessfulLogin(user.getIdUser());

            // First login after registration — skip OTP, issue JWT directly
            if (user.getSkipNextOtp() != null && user.getSkipNextOtp() == 1) {
                appUserRepository.clearSkipNextOtp(user.getIdUser());

                Optional<AppUser> fullUser = appUserRepository.findByUsernameWithRelations(user.getUsername());
                if (fullUser.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("success", false, "message", "User not found"));
                }

                AppUser u = fullUser.get();
                String accessToken = jwtUtil.generateAccessToken(u);
                String refreshToken = jwtUtil.generateRefreshToken(u);

                AppUserResponseDTO userDTO = appUserService.getUserById(u.getIdUser())
                        .orElseThrow(() -> new IllegalStateException("User not found after login"));

                log.info("First login after registration — JWT issued directly for user: {}", u.getUsername());
                return ResponseEntity.ok(LoginResponseDTO.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .tokenType("Bearer")
                        .expiresIn(jwtExpirationMs / 1000)
                        .user(userDTO)
                        .build());
            }

            // Normal login — send OTP
            Optional<String> emailOpt = appUserRepository.findEmailByUsername(user.getUsername());
            if (emailOpt.isEmpty()) {
                log.warn("No email found for user: {} — cannot send OTP", user.getUsername());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "2FA nije dostupan za ovaj nalog. Kontaktirajte administratora."));
            }

            twoFactorAuthService.generateAndSendOtp(emailOpt.get());

            log.info("OTP sent to {} for user: {}", emailOpt.get(), user.getUsername());
            return ResponseEntity.ok(Map.of(
                    "status", "OTP_REQUIRED",
                    "message", "Kod za verifikaciju je poslat na Vašu email adresu"
            ));

        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An error occurred during login"));
        }
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "User login - step 2", description = "Validates OTP and issues JWT token")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerifyRequestDTO request) {
        try {
            log.info("OTP verification attempt for username: {}", request.getUsername());

            Optional<AppUser> userOptional = appUserService.authenticate(request.getUsername(), "");
            // We need the entity — load it via username with relations
            Optional<String> emailOpt = appUserRepository.findEmailByUsername(request.getUsername());

            if (emailOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Korisnik nije pronađen"));
            }

            boolean valid = twoFactorAuthService.validateOtp(emailOpt.get(), request.getOtp());
            if (!valid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Neispravan ili istekao kod"));
            }

            // OTP valid — now issue JWT
            // Load full user entity with relations for token generation
            Optional<AppUser> fullUser = appUserRepository.findByUsernameWithRelations(request.getUsername());
            if (fullUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Korisnik nije pronađen"));
            }

            AppUser user = fullUser.get();
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            AppUserResponseDTO userDTO = appUserService.getUserById(user.getIdUser())
                    .orElseThrow(() -> new IllegalStateException("User not found after OTP verify"));

            LoginResponseDTO response = LoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpirationMs / 1000)
                    .user(userDTO)
                    .build();

            log.info("OTP verified — JWT issued for user: {}", user.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error during OTP verification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An error occurred during OTP verification"));
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