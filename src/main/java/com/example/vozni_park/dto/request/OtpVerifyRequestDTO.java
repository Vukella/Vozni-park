package com.example.vozni_park.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpVerifyRequestDTO {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "OTP code is required")
    private String otp;
}