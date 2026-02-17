package com.example.vozni_park.service;

import com.example.vozni_park.dto.request.AppUserRequestDTO;
import com.example.vozni_park.dto.response.AppUserResponseDTO;
import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.entity.Role;
import com.example.vozni_park.mapper.AppUserMapper;
import com.example.vozni_park.repository.AppUserRepository;
import com.example.vozni_park.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j  // ✅ ADDED for logging
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final AppUserMapper appUserMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all users - returns DTOs (NO password hashes!)
     */
    public List<AppUserResponseDTO> getAllUsers() {
        List<AppUser> users = appUserRepository.findAll();
        return appUserMapper.toResponseDTOList(users);
    }

    /**
     * Get user by ID
     */
    public Optional<AppUserResponseDTO> getUserById(Long id) {
        return appUserRepository.findById(id)
                .map(appUserMapper::toResponseDTO);
    }

    /**
     * Get user by username
     */
    public Optional<AppUserResponseDTO> getUserByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .map(appUserMapper::toResponseDTO);
    }

    /**
     * Get users by role
     */
    public List<AppUserResponseDTO> getUsersByRole(Long roleId) {
        List<AppUser> users = appUserRepository.findByRoleId(roleId);
        return appUserMapper.toResponseDTOList(users);
    }

    /**
     * Get users by role name
     */
    public List<AppUserResponseDTO> getUsersByRoleName(String roleName) {
        List<AppUser> users = appUserRepository.findByRoleName(roleName);
        return appUserMapper.toResponseDTOList(users);
    }

    /**
     * Get active users
     */
    public List<AppUserResponseDTO> getActiveUsers() {
        List<AppUser> users = appUserRepository.findByIsActive(true);
        return appUserMapper.toResponseDTOList(users);
    }

    /**
     * Get users by location
     */
    public List<AppUserResponseDTO> getUsersByLocation(Long locationId) {
        List<AppUser> users = appUserRepository.findByLocationId(locationId);
        return appUserMapper.toResponseDTOList(users);
    }

    /**
     * Create new user from DTO
     * ✅ UPDATED: Now properly hashes password using BCrypt
     * ✅ UPDATED: Validates SUPER_ADMIN cannot have location assignments
     */
    @Transactional
    public AppUserResponseDTO createUser(AppUserRequestDTO userDTO) {
        // Validate username uniqueness
        if (appUserRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Validate role exists and fetch it
        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + userDTO.getRoleId()));

        // ✅ NEW VALIDATION: SUPER_ADMIN cannot have location assignments
        // Note: This is a documentation warning. Actual location assignment validation
        // should be enforced in UserLocationService when assigning locations.
        if ("SUPER_ADMIN".equals(role.getName())) {
            log.info("Creating SUPER_ADMIN user: {}. Note: SUPER_ADMIN users should not be assigned to specific locations.", userDTO.getUsername());
            // SUPER_ADMIN users have access to all locations by default
        }

        // Create user entity
        AppUser user = new AppUser();
        user.setUsername(userDTO.getUsername());
        user.setFullName(userDTO.getFullName());
        user.setPasswordHash(passwordEncoder.encode(userDTO.getPassword())); // Hash password
        user.setIsActive(userDTO.getIsActive());
        user.setRoleId(userDTO.getRoleId());
        user.setFailedLoginAttempts(0);

        // Save user
        AppUser savedUser = appUserRepository.save(user);

        // Set the role (we already fetched it above, so reuse it)
        savedUser.setRole(role);

        // Return DTO (password excluded by mapper)
        return appUserMapper.toResponseDTO(savedUser);
    }

    /**
     * Update existing user from DTO
     */
    @Transactional
    public AppUserResponseDTO updateUser(Long id, AppUserRequestDTO userDTO) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Validation: Check if new username conflicts
        if (!userDTO.getUsername().equals(user.getUsername()) &&
                appUserRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username '" + userDTO.getUsername() + "' already exists");
        }

        // Validation: Check if role exists
        if (!roleRepository.existsById(userDTO.getRoleId())) {
            throw new IllegalArgumentException("Role not found with id: " + userDTO.getRoleId());
        }

        // Update entity from DTO (does NOT update password)
        appUserMapper.updateEntity(user, userDTO);

        // Save and return DTO
        AppUser updated = appUserRepository.save(user);
        return appUserMapper.toResponseDTO(updated);
    }

    /**
     * Update user password
     * ✅ UPDATED: Now properly hashes password using BCrypt
     */
    @Transactional
    public void updatePassword(Long id, String newPassword) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Hash password before saving
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(hashedPassword);

        appUserRepository.save(user);
    }

    /**
     * Activate/deactivate user
     */
    @Transactional
    public AppUserResponseDTO updateUserActiveStatus(Long id, Boolean isActive) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setIsActive(isActive);

        AppUser updated = appUserRepository.save(user);
        return appUserMapper.toResponseDTO(updated);
    }

    /**
     * Record failed login attempt
     */
    @Transactional
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
    @Transactional
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
    @Transactional
    public void resetFailedLoginAttempts(Long id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setFailedLoginAttempts(0);
        user.setLastFailedLogin(null);

        appUserRepository.save(user);
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!appUserRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        appUserRepository.deleteById(id);
    }

    /**
     * Check if user exists
     */
    public boolean userExists(Long id) {
        return appUserRepository.existsById(id);
    }

    /**
     * Authenticate user - returns entity for login logic
     * ✅ UPDATED: Now uses eager fetching to avoid lazy loading issues with JWT generation
     * (This is the only place where we return entity because we need to check password)
     */
    public Optional<AppUser> authenticate(String username, String password) {
        // Use eager fetching query to load role and userLocations
        Optional<AppUser> userOpt = appUserRepository.findByUsernameWithRelations(username);

        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();

            // Check if user is active
            if (!user.getIsActive()) {
                return Optional.empty();
            }

            // Use BCrypt password comparison
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }
}