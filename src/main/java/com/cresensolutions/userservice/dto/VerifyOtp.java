package com.cresensolutions.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtp {
    @NotBlank
    private String email;
    @NotBlank
    private String otp;
}
