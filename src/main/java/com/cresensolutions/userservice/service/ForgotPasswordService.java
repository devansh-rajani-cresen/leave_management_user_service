package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.SendOtp;
import com.cresensolutions.userservice.dto.VerifyOtp;

public interface ForgotPasswordService {
        void sendOtp(SendOtp sendOtpRequest);
        void verifyOtp(VerifyOtp verifyOtpRequest);
}