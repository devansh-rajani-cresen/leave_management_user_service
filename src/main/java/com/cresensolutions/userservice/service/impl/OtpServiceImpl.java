package com.cresensolutions.userservice.service.impl;

import com.cresensolutions.userservice.service.OtpService;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import static com.cresensolutions.userservice.common.UserConstants.OTP_LENGTH;

@Service
public class OtpServiceImpl implements OtpService {
    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateOtp() {
        int otp = secureRandom.nextInt((int) Math.pow(10, OTP_LENGTH));
        return String.format("%0" + OTP_LENGTH + "d", otp);
    }
}