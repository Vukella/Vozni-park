package com.example.vozni_park.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverRequestDTO {

    @NotNull(message = "SAP number is required")
    private Long sapNumber;

    @NotBlank(message = "Full name is required")
    @Size(max = 60, message = "Full name must not exceed 60 characters")
    private String fullName;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Pattern(regexp = "^[+]?[0-9\\s-()]*$", message = "Invalid phone number format")
    private String phone;

    @Size(max = 30, message = "Status must not exceed 30 characters")
    private String status;

    private Integer statusCode;
}

