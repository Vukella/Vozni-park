package com.example.vozni_park.security;

import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.entity.UserLocation;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for JWT token operations.
 * Handles token generation, validation, and claims extraction.
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long jwtRefreshExpirationMs;

    /**
     * Generate JWT access token from AppUser entity
     */
    public String generateAccessToken(AppUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getIdUser());
        claims.put("username", user.getUsername());
        claims.put("fullName", user.getFullName());
        claims.put("roleId", user.getRole().getIdRole());
        claims.put("roleName", user.getRole().getName());

        // ✅ FIXED: Use getLocation() instead of getLocationUnit()
        List<Long> locationIds = user.getUserLocations() != null
                ? user.getUserLocations().stream()
                .map(ul -> ul.getLocation().getIdLocationUnit())
                .collect(Collectors.toList())
                : List.of();
        claims.put("locationIds", locationIds);

        return createToken(claims, user.getUsername(), jwtExpirationMs);
    }

    /**
     * Generate JWT refresh token from AppUser entity
     */
    public String generateRefreshToken(AppUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getIdUser());
        claims.put("username", user.getUsername());
        claims.put("tokenType", "refresh");

        return createToken(claims, user.getUsername(), jwtRefreshExpirationMs);
    }

    /**
     * Create JWT token with claims and expiration
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extract role name from token
     */
    public String extractRoleName(String token) {
        return extractClaim(token, claims -> claims.get("roleName", String.class));
    }

    /**
     * Extract location IDs from token
     * ✅ FIXED: Proper type conversion handling
     */
    @SuppressWarnings("unchecked")
    public List<Long> extractLocationIds(String token) {
        return extractClaim(token, claims -> {
            Object locationIds = claims.get("locationIds");
            if (locationIds instanceof List<?>) {
                return ((List<?>) locationIds).stream()
                        .map(obj -> {
                            if (obj instanceof Integer) {
                                return Long.valueOf((Integer) obj);
                            } else if (obj instanceof Long) {
                                return (Long) obj;
                            }
                            return null;
                        })
                        .filter(id -> id != null)
                        .collect(Collectors.toList());
            }
            return List.of();
        });
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, ClaimsResolver<T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.resolve(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if token is expired
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate token against username and expiration
     */
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * Validate token (checks signature and expiration)
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Functional interface for claims resolution
     */
    @FunctionalInterface
    public interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
}