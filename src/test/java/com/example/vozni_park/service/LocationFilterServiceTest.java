package com.example.vozni_park.service;

import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.entity.LocationUnit;
import com.example.vozni_park.entity.Role;
import com.example.vozni_park.entity.UserLocation;
import com.example.vozni_park.repository.AppUserRepository;
import com.example.vozni_park.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationFilterServiceTest {

    @Mock private AppUserRepository appUserRepository;

    @InjectMocks
    private LocationFilterService locationFilterService;

    @AfterEach
    void clearSecurityContext() {
        // Always clean up after each test — security context is global
        SecurityContextHolder.clearContext();
    }

    // --- Helper to set up security context ---

    private void authenticateAs(String roleName, AppUser user) {
        Role role = new Role();
        role.setName(roleName);
        user.setRole(role);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + roleName))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private AppUser buildUser(Long id) {
        AppUser user = new AppUser();
        user.setIdUser(id);
        user.setUsername("user" + id);
        user.setIsActive(true);
        user.setFailedLoginAttempts(0);
        return user;
    }

    private LocationUnit buildLocation(Long id) {
        LocationUnit loc = new LocationUnit();
        loc.setIdLocationUnit(id);
        return loc;
    }

    // --- isSuperAdmin() tests ---

    @Test
    void isSuperAdmin_withSuperAdminRole_returnsTrue() {
        authenticateAs("SUPER_ADMIN", buildUser(1L));

        assertThat(locationFilterService.isSuperAdmin()).isTrue();
    }

    @Test
    void isSuperAdmin_withLocalAdminRole_returnsFalse() {
        authenticateAs("LOCAL_ADMIN", buildUser(1L));

        assertThat(locationFilterService.isSuperAdmin()).isFalse();
    }

    // --- isLocalAdmin() tests ---

    @Test
    void isLocalAdmin_withLocalAdminRole_returnsTrue() {
        authenticateAs("LOCAL_ADMIN", buildUser(1L));

        assertThat(locationFilterService.isLocalAdmin()).isTrue();
    }

    // --- getCurrentUserLocationIds() tests ---

    @Test
    void getCurrentUserLocationIds_superAdmin_returnsEmptyListWithoutHittingDatabase() {
        authenticateAs("SUPER_ADMIN", buildUser(1L));

        List<Long> result = locationFilterService.getCurrentUserLocationIds();

        assertThat(result).isEmpty();
        // SUPER_ADMIN must never query the database for location filtering
        verifyNoInteractions(appUserRepository);
    }

    @Test
    void getCurrentUserLocationIds_localAdminWithTwoLocations_returnsBothIds() {
        AppUser user = buildUser(2L);

        UserLocation ul1 = new UserLocation();
        ul1.setLocation(buildLocation(10L));
        UserLocation ul2 = new UserLocation();
        ul2.setLocation(buildLocation(20L));
        user.setUserLocations(List.of(ul1, ul2));

        authenticateAs("LOCAL_ADMIN", user);
        when(appUserRepository.findByIdWithRelations(2L)).thenReturn(Optional.of(user));

        List<Long> result = locationFilterService.getCurrentUserLocationIds();

        assertThat(result).containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    void getCurrentUserLocationIds_localAdminWithNoLocations_throwsException() {
        AppUser user = buildUser(3L);
        user.setUserLocations(List.of());

        authenticateAs("LOCAL_ADMIN", user);
        when(appUserRepository.findByIdWithRelations(3L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> locationFilterService.getCurrentUserLocationIds())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must have at least one assigned location");
    }

    // --- hasAccessToLocation() tests ---

    @Test
    void hasAccessToLocation_superAdmin_alwaysReturnsTrue() {
        authenticateAs("SUPER_ADMIN", buildUser(1L));

        // SUPER_ADMIN has access to any location, even one that doesn't exist
        assertThat(locationFilterService.hasAccessToLocation(999L)).isTrue();
    }

    @Test
    void hasAccessToLocation_localAdminWithMatchingLocation_returnsTrue() {
        AppUser user = buildUser(2L);

        UserLocation ul = new UserLocation();
        ul.setLocation(buildLocation(10L));
        user.setUserLocations(List.of(ul));

        authenticateAs("LOCAL_ADMIN", user);
        when(appUserRepository.findByIdWithRelations(2L)).thenReturn(Optional.of(user));

        assertThat(locationFilterService.hasAccessToLocation(10L)).isTrue();
    }

    @Test
    void hasAccessToLocation_localAdminWithoutMatchingLocation_returnsFalse() {
        AppUser user = buildUser(2L);

        UserLocation ul = new UserLocation();
        ul.setLocation(buildLocation(10L));
        user.setUserLocations(List.of(ul));

        authenticateAs("LOCAL_ADMIN", user);
        when(appUserRepository.findByIdWithRelations(2L)).thenReturn(Optional.of(user));

        assertThat(locationFilterService.hasAccessToLocation(99L)).isFalse();
    }

    // --- validateLocationAccess() tests ---

    @Test
    void validateLocationAccess_localAdminWithoutAccess_throwsSecurityException() {
        AppUser user = buildUser(2L);

        UserLocation ul = new UserLocation();
        ul.setLocation(buildLocation(10L));
        user.setUserLocations(List.of(ul));

        authenticateAs("LOCAL_ADMIN", user);
        when(appUserRepository.findByIdWithRelations(2L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> locationFilterService.validateLocationAccess(99L))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Access denied");
    }
}