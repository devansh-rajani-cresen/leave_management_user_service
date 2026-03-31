package com.cresensolutions.userservice.dto;

import lombok.Data;

@Data
public class VerifyOtp {
    private String email;
    private String otp;
}
