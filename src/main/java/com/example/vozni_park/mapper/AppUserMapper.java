package com.example.vozni_park.mapper;

import com.example.vozni_park.dto.request.AppUserRequestDTO;
import com.example.vozni_park.dto.response.AppUserResponseDTO;
import com.example.vozni_park.dto.summary.AppUserSummaryDTO;
import com.example.vozni_park.dto.summary.LocationUnitSummaryDTO;
import com.example.vozni_park.dto.summary.RoleSummaryDTO;
import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.entity.LocationUnit;
import com.example.vozni_park.entity.Role;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AppUserMapper {

    /**
     * Convert AppUser entity to AppUserResponseDTO
     * NEVER include password hash in response!
     */
    public AppUserResponseDTO toResponseDTO(AppUser user) {
        if (user == null) {
            return null;
        }

        AppUserResponseDTO dto = new AppUserResponseDTO();
        dto.setIdUser(user.getIdUser());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setFailedLoginAttempts(user.getFailedLoginAttempts());
        dto.setLastFailedLogin(user.getLastFailedLogin());
        dto.setLastSuccessfulLogin(user.getLastSuccessfulLogin());
        dto.setIsActive(user.getIsActive());

        // Map Role (if present)
        if (user.getRole() != null) {
            Role role = user.getRole();
            dto.setRole(new RoleSummaryDTO(
                    role.getIdRole(),
                    role.getName()
            ));
        }

        // Map Locations (if present)
        if (user.getUserLocations() != null && !user.getUserLocations().isEmpty()) {
            List<LocationUnitSummaryDTO> locations = user.getUserLocations().stream()
                    .map(userLocation -> {
                        LocationUnit location = userLocation.getLocation();
                        return new LocationUnitSummaryDTO(
                                location.getIdLocationUnit(),
                                location.getLocationName(),
                                location.getLocationAddress()
                        );
                    })
                    .collect(Collectors.toList());
            dto.setLocations(locations);
        } else {
            dto.setLocations(new ArrayList<>());
        }

        return dto;
    }

    /**
     * Convert AppUserRequestDTO to AppUser entity
     * Password will be hashed by service layer
     */
    public AppUser toEntity(AppUserRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        AppUser user = new AppUser();
        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setIsActive(dto.getIsActive());
        user.setRoleId(dto.getRoleId());
        user.setFailedLoginAttempts(0);

        // Password will be hashed by service layer before saving
        // Do NOT set passwordHash directly from DTO

        return user;
    }

    /**
     * Update existing AppUser entity from AppUserRequestDTO
     * Does NOT update password - use separate method for password changes
     */
    public void updateEntity(AppUser user, AppUserRequestDTO dto) {
        if (user == null || dto == null) {
            return;
        }

        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setIsActive(dto.getIsActive());
        user.setRoleId(dto.getRoleId());

        // Password update is handled separately by service layer
    }

    /**
     * Convert list of AppUser entities to list of AppUserResponseDTOs
     */
    public List<AppUserResponseDTO> toResponseDTOList(List<AppUser> users) {
        if (users == null) {
            return new ArrayList<>();
        }
        return users.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert AppUser to AppUserSummaryDTO (lightweight)
     */
    public AppUserSummaryDTO toSummaryDTO(AppUser user) {
        if (user == null) {
            return null;
        }

        AppUserSummaryDTO dto = new AppUserSummaryDTO();
        dto.setIdUser(user.getIdUser());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());

        if (user.getRole() != null) {
            Role role = user.getRole();
            dto.setRole(new RoleSummaryDTO(
                    role.getIdRole(),
                    role.getName()
            ));
        }

        return dto;
    }
}

