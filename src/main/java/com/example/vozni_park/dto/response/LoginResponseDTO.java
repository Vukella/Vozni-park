package com.example.vozni_park.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login response containing JWT tokens and user information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    /**
     * JWT access token (short-lived, 15 minutes)
     */
    private String accessToken;

    /**
     * JWT refresh token (long-lived, 7 days)
     */
    private String refreshToken;

    /**
     * Token type (always "Bearer")
     */
    private String tokenType = "Bearer";

    /**
     * Access token expiration time in seconds
     */
    private long expiresIn;

    /**
     * User information (without password)
     */
    private AppUserResponseDTO user;
}