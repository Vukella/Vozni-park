package com.example.vozni_park.security;

import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.entity.UserLocation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom UserDetails implementation that wraps AppUser entity
 * for Spring Security authentication.
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final AppUser user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert role name to Spring Security authority
        // Role name in DB: "SUPER_ADMIN" -> Authority: "ROLE_SUPER_ADMIN"
        String roleName = user.getRole().getName();
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // We don't track account expiration
    }

    @Override
    public boolean isAccountNonLocked() {
        // Account is locked if failed attempts >= 5
        return user.getFailedLoginAttempts() < 5;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // We don't track password expiration
    }

    @Override
    public boolean isEnabled() {
        return user.getIsActive();
    }

    // Custom getters for additional user info
    public Long getUserId() {
        return user.getIdUser();
    }

    public String getFullName() {
        return user.getFullName();
    }

    public String getRoleName() {
        return user.getRole().getName();
    }

    public Long getRoleId() {
        return user.getRole().getIdRole();
    }

    // ✅ FIXED: Use getLocation() instead of getLocationUnit()
    public List<Long> getLocationIds() {
        if (user.getUserLocations() == null) {
            return List.of();
        }
        return user.getUserLocations().stream()
                .map(ul -> ul.getLocation().getIdLocationUnit())
                .collect(Collectors.toList());
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(getRoleName());
    }

    public boolean isLocalAdmin() {
        return "LOCAL_ADMIN".equals(getRoleName());
    }
}