package com.cresensolutions.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendOtp {
    @NotBlank
    private String email;
}
