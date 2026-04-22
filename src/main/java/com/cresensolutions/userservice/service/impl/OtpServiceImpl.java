package com.cresensolutions.userservice.service;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class OtpServiceImpl implements OtpService {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int OTP_LENGTH = 6;

    @Override
    public String generateOtp() {
        int otp = secureRandom.nextInt((int) Math.pow(10, OTP_LENGTH));
        return String.format("%0" + OTP_LENGTH + "d", otp);
    }
}