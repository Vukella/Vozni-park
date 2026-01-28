package com.example.vozni_park.dto.response;

import com.example.vozni_park.dto.summary.LocationUnitSummaryDTO;
import com.example.vozni_park.dto.summary.RoleSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUserResponseDTO {
    private Long idUser;
    private String username;
    private String fullName;
    private Integer failedLoginAttempts;
    private LocalDateTime lastFailedLogin;
    private LocalDateTime lastSuccessfulLogin;
    private Boolean isActive;

    // Nested objects (NO password hash!)
    private RoleSummaryDTO role;
    private List<LocationUnitSummaryDTO> locations;
}

