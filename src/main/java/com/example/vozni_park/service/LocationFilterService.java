package com.example.vozni_park.service;

import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.entity.LocationUnit;
import com.example.vozni_park.entity.UserLocation;
import com.example.vozni_park.repository.AppUserRepository;
import com.example.vozni_park.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling location-based filtering logic.
 * Automatically filters data based on user's role and assigned locations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationFilterService {

    private final AppUserRepository appUserRepository;

    /**
     * Check if current user is SUPER_ADMIN (sees all locations)
     */
    public boolean isSuperAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    /**
     * Check if current user is LOCAL_ADMIN (location-restricted)
     */
    public boolean isLocalAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_LOCAL_ADMIN"));
    }

    /**
     * Get current user's ID from security context
     */
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new IllegalStateException("No authenticated user found");
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getUserId();
    }

    /**
     * Get location IDs assigned to current user.
     * Returns empty list for SUPER_ADMIN (they see all locations).
     * Throws exception if LOCAL_ADMIN has no assigned locations.
     */
    @Transactional(readOnly = true)
    public List<Long> getCurrentUserLocationIds() {
        // SUPER_ADMIN sees everything - return empty list to bypass filtering
        if (isSuperAdmin()) {
            log.debug("User is SUPER_ADMIN - no location filtering");
            return Collections.emptyList();
        }

        // Get current user with locations
        Long userId = getCurrentUserId();
        AppUser user = appUserRepository.findByIdWithRelations(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        // Extract location IDs (using correct field name)
        List<Long> locationIds = user.getUserLocations().stream()
                .map(UserLocation::getLocation)  // ✅ FIXED: getLocation(), not getLocationUnit()
                .map(LocationUnit::getIdLocationUnit)
                .collect(Collectors.toList());

        // LOCAL_ADMIN must have at least one location
        if (isLocalAdmin() && locationIds.isEmpty()) {
            log.error("LOCAL_ADMIN user {} has no assigned locations", userId);
            throw new IllegalStateException("LOCAL_ADMIN user must have at least one assigned location");
        }

        log.debug("User {} has access to {} location(s): {}", userId, locationIds.size(), locationIds);
        return locationIds;
    }

    /**
     * Check if current user has access to a specific location.
     * SUPER_ADMIN always returns true.
     */
    @Transactional(readOnly = true)
    public boolean hasAccessToLocation(Long locationId) {
        if (isSuperAdmin()) {
            return true;
        }

        List<Long> userLocationIds = getCurrentUserLocationIds();
        return userLocationIds.contains(locationId);
    }

    /**
     * Validate that current user can access a specific location.
     * Throws exception if access denied.
     */
    @Transactional(readOnly = true)
    public void validateLocationAccess(Long locationId) {
        if (!hasAccessToLocation(locationId)) {
            throw new SecurityException("Access denied to location: " + locationId);
        }
    }

    /**
     * Get current username for logging
     */
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }
}