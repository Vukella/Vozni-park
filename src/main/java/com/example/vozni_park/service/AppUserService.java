package com.example.vozni_park.service;

import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.repository.AppUserRepository;
import com.example.vozni_park.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AppUserService {
    
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    
    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<AppUser> getAllUsers() {
        return appUserRepository.findAll();
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public Optional<AppUser> getUserById(Long id) {
        return appUserRepository.findById(id);
    }
    
    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public Optional<AppUser> getUserByUsername(String username) {
        return appUserRepository.findByUsername(username);
    }
    
    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    public List<AppUser> getUsersByRole(Long roleId) {
        return appUserRepository.findByRoleId(roleId);
    }
    
    /**
     * Get users by role name
     */
    @Transactional(readOnly = true)
    public List<AppUser> getUsersByRoleName(String roleName) {
        return appUserRepository.findByRoleName(roleName);
    }
    
    /**
     * Get active users
     */
    @Transactional(readOnly = true)
    public List<AppUser> getActiveUsers() {
        return appUserRepository.findByIsActive(true);
    }
    
    /**
     * Get users by location
     */
    @Transactional(readOnly = true)
    public List<AppUser> getUsersByLocation(Long locationId) {
        return appUserRepository.findByLocationId(locationId);
    }
    
    /**
     * Create new user
     */
    public AppUser createUser(AppUser user) {
        // Validation: Check if username already exists
        if (appUserRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username '" + user.getUsername() + "' already exists");
        }
        
        // Validation: Check if role exists
        if (!roleRepository.existsById(user.getRoleId())) {
            throw new IllegalArgumentException("Role not found with id: " + user.getRoleId());
        }
        
        // TODO: Hash password before saving (implement password hashing)
        // user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        
        // Set default values
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        if (user.getFailedLoginAttempts() == null) {
            user.setFailedLoginAttempts(0);
        }
        
        return appUserRepository.save(user);
    }
    
    /**
     * Update existing user
     */
    public AppUser updateUser(Long id, AppUser userDetails) {
        AppUser user = appUserRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        // Validation: Check if new username conflicts
        if (!userDetails.getUsername().equals(user.getUsername()) &&
            appUserRepository.existsByUsername(userDetails.getUsername())) {
            throw new IllegalArgumentException("Username '" + userDetails.getUsername() + "' already exists");
        }
        
        // Validation: Check if role exists
        if (!roleRepository.existsById(userDetails.getRoleId())) {
            throw new IllegalArgumentException("Role not found with id: " + userDetails.getRoleId());
        }
        
        // Update fields
        user.setUsername(userDetails.getUsername());
        user.setFullName(userDetails.getFullName());
        user.setRoleId(userDetails.getRoleId());
        user.setIsActive(userDetails.getIsActive());
        
        // Note: Don't update password here - use separate method
        
        return appUserRepository.save(user);
    }
    
    /**
     * Update user password
     */
    public void updatePassword(Long id, String newPassword) {
        AppUser user = appUserRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        // TODO: Hash password before saving
        // user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordHash(newPassword); // Temporary - replace with hashed password
        
        appUserRepository.save(user);
    }
    
    /**
     * Activate/deactivate user
     */
    public AppUser updateUserActiveStatus(Long id, Boolean isActive) {
        AppUser user = appUserRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        user.setIsActive(isActive);
        
        return appUserRepository.save(user);
    }
    
    /**
     * Record failed login attempt
     */
    public void recordFailedLogin(Long id) {
        AppUser user = appUserRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        user.setLastFailedLogin(LocalDateTime.now());
        
        // Auto-deactivate after 5 failed attempts
        if (user.getFailedLoginAttempts() >= 5) {
            user.setIsActive(false);
        }
        
        appUserRepository.save(user);
    }
    
    /**
     * Record successful login
     */
    public void recordSuccessfulLogin(Long id) {
        AppUser user = appUserRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        user.setFailedLoginAttempts(0);
        user.setLastSuccessfulLogin(LocalDateTime.now());
        
        appUserRepository.save(user);
    }
    
    /**
     * Reset failed login attempts
     */
    public void resetFailedLoginAttempts(Long id) {
        AppUser user = appUserRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        user.setFailedLoginAttempts(0);
        user.setLastFailedLogin(null);
        
        appUserRepository.save(user);
    }
    
    /**
     * Delete user by ID
     */
    public void deleteUser(Long id) {
        if (!appUserRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        
        // TODO: Check if user has created any travel orders before deleting
        
        appUserRepository.deleteById(id);
    }
    
    /**
     * Check if user exists
     */
    @Transactional(readOnly = true)
    public boolean userExists(Long id) {
        return appUserRepository.existsById(id);
    }
    
    /**
     * Authenticate user (basic - should be replaced with Spring Security)
     */
    @Transactional(readOnly = true)
    public Optional<AppUser> authenticate(String username, String password) {
        Optional<AppUser> userOpt = appUserRepository.findByUsername(username);
        
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            
            // Check if user is active
            if (!user.getIsActive()) {
                return Optional.empty();
            }
            
            // TODO: Use proper password hashing comparison
            // if (passwordEncoder.matches(password, user.getPasswordHash()))
            if (password.equals(user.getPasswordHash())) {
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }
}
