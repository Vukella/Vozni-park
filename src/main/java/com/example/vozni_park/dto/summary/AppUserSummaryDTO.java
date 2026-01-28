package com.example.vozni_park.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUserSummaryDTO {
    private Long idUser;
    private String username;
    private String fullName;
    private RoleSummaryDTO role;
}

