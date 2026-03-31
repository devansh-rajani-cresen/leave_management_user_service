package com.cresensolutions.userservice.service;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @InjectMocks
    private OtpService otpService;

    // OTP should not be null
    @Test
    void generateOtp_ShouldNotBeNull() {
        String otp = otpService.generateOtp();
        assertNotNull(otp);
    }

    // OTP should always be exactly 6 digits
    @Test
    void generateOtp_ShouldBeExactlySixDigits() {
        String otp = otpService.generateOtp();
        assertEquals(6, otp.length());
    }

    // OTP should only contain numeric characters
    @Test
    void generateOtp_ShouldBeNumericOnly() {
        String otp = otpService.generateOtp();
        assertTrue(otp.matches("\\d{6}"), "OTP should contain digits only");
    }

    // OTP should be in valid range (100000 - 999999)
    @Test
    void generateOtp_ShouldBeInValidRange() {
        String otp = otpService.generateOtp();
        int otpValue = Integer.parseInt(otp);
        assertTrue(otpValue >= 100000 && otpValue <= 999999);
    }

    // OTP should not start with 0 (since range starts from 100000)
    @Test
    void generateOtp_ShouldNotStartWithZero() {
        String otp = otpService.generateOtp();
        assertNotEquals('0', otp.charAt(0));
    }

    // Run 10 times to verify randomness and consistency
    @RepeatedTest(10)
    void generateOtp_ShouldAlwaysProduceValidOtp() {
        String otp = otpService.generateOtp();
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }
}