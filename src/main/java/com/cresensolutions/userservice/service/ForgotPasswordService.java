package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.SendOtp;
import com.cresensolutions.userservice.dto.VerifyOtp;

public interface ForgotPasswordService {
        boolean sendOtp(SendOtp sendOtpRequest);
        boolean verifyOtp(VerifyOtp verifyOtpRequest);
}