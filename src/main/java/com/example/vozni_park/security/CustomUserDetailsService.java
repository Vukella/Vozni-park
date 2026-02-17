package com.example.vozni_park.security;

import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService implementation for loading user-specific data.
 * Used by Spring Security for authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    /**
     * Load user by username for authentication.
     * Uses eager fetching to avoid lazy loading issues in stateless JWT authentication.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        // CRITICAL: Use findByUsernameWithRelations to eagerly fetch role and locations
        // This is essential for stateless JWT authentication where there's no session
        AppUser user = appUserRepository.findByUsernameWithRelations(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        // Validate that role is loaded
        if (user.getRole() == null) {
            log.error("User role not loaded for username: {}", username);
            throw new IllegalStateException("User role not loaded");
        }

        log.debug("User loaded successfully: {} with role: {}", username, user.getRole().getName());

        return new CustomUserDetails(user);
    }

    /**
     * Load user by ID for token validation.
     * Uses eager fetching to avoid lazy loading issues.
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) {
        log.debug("Loading user by ID: {}", userId);

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new UsernameNotFoundException("User not found with ID: " + userId);
                });

        // For findById, we need to manually trigger loading of relationships
        // to ensure they're available after transaction ends
        if (user.getRole() == null) {
            log.error("User role not loaded for ID: {}", userId);
            throw new IllegalStateException("User role not loaded");
        }

        // Trigger loading of locations
        user.getUserLocations().size();

        log.debug("User loaded successfully with ID: {} and role: {}", userId, user.getRole().getName());

        return new CustomUserDetails(user);
    }
}