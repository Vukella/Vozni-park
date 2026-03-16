package com.example.vozni_park.service;

import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.entity.Role;
import com.example.vozni_park.mapper.AppUserMapper;
import com.example.vozni_park.repository.AppUserRepository;
import com.example.vozni_park.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock private AppUserRepository appUserRepository;
    @Mock private AppUserMapper appUserMapper;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserService appUserService;

    private AppUser activeUser;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName("LOCAL_ADMIN");

        activeUser = new AppUser();
        activeUser.setIdUser(1L);
        activeUser.setUsername("testuser");
        activeUser.setPasswordHash("$2a$10$hashedpassword");
        activeUser.setIsActive(true);
        activeUser.setFailedLoginAttempts(0);
        activeUser.setRole(role);
    }

    // --- authenticate() tests ---

    @Test
    void authenticate_withCorrectPassword_returnsUser() {
        when(appUserRepository.findByUsernameWithRelations("testuser"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("correctpassword", activeUser.getPasswordHash()))
                .thenReturn(true);

        Optional<AppUser> result = appUserService.authenticate("testuser", "correctpassword");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void authenticate_withWrongPassword_returnsEmpty() {
        when(appUserRepository.findByUsernameWithRelations("testuser"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrongpassword", activeUser.getPasswordHash()))
                .thenReturn(false);

        Optional<AppUser> result = appUserService.authenticate("testuser", "wrongpassword");

        assertThat(result).isEmpty();
    }

    @Test
    void authenticate_withInactiveUser_returnsEmpty() {
        activeUser.setIsActive(false);
        when(appUserRepository.findByUsernameWithRelations("testuser"))
                .thenReturn(Optional.of(activeUser));

        Optional<AppUser> result = appUserService.authenticate("testuser", "anypassword");

        assertThat(result).isEmpty();
        // Password check must be skipped entirely for inactive users
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void authenticate_withNonExistentUsername_returnsEmpty() {
        when(appUserRepository.findByUsernameWithRelations("unknown"))
                .thenReturn(Optional.empty());

        Optional<AppUser> result = appUserService.authenticate("unknown", "anypassword");

        assertThat(result).isEmpty();
    }

    // --- recordFailedLogin() tests ---

    @Test
    void recordFailedLogin_incrementsFailedAttemptCounter() {
        activeUser.setFailedLoginAttempts(2);
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        appUserService.recordFailedLogin(1L);

        assertThat(activeUser.getFailedLoginAttempts()).isEqualTo(3);
        assertThat(activeUser.getIsActive()).isTrue();
        verify(appUserRepository).save(activeUser);
    }

    @Test
    void recordFailedLogin_atFiveAttempts_deactivatesUser() {
        activeUser.setFailedLoginAttempts(4);
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        appUserService.recordFailedLogin(1L);

        assertThat(activeUser.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(activeUser.getIsActive()).isFalse();
        verify(appUserRepository).save(activeUser);
    }

    @Test
    void recordFailedLogin_belowFiveAttempts_userRemainsActive() {
        activeUser.setFailedLoginAttempts(3);
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        appUserService.recordFailedLogin(1L);

        assertThat(activeUser.getFailedLoginAttempts()).isEqualTo(4);
        assertThat(activeUser.getIsActive()).isTrue();
    }

    // --- recordSuccessfulLogin() tests ---

    @Test
    void recordSuccessfulLogin_resetsFailedAttempts() {
        activeUser.setFailedLoginAttempts(3);
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        appUserService.recordSuccessfulLogin(1L);

        assertThat(activeUser.getFailedLoginAttempts()).isEqualTo(0);
        verify(appUserRepository).save(activeUser);
    }

    // --- deleteUser() tests ---

    @Test
    void deleteUser_withNonExistentId_throwsException() {
        when(appUserRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> appUserService.deleteUser(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }
}