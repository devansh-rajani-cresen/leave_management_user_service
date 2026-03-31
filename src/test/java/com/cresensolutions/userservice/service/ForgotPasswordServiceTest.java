package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.SendOtp;
import com.cresensolutions.userservice.dto.VerifyOtp;
import com.cresensolutions.userservice.entity.Otp;
import com.cresensolutions.userservice.repository.OtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ForgotPasswordServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private ForgotPasswordService forgotPasswordService;

    private SendOtp sendOtpRequest;
    private VerifyOtp verifyOtpRequest;

    private final String email = "demo@gmail.com";
    private final String otp = "123456";

    @BeforeEach
    void setUp() {
        // Setup SendOtp DTO
        sendOtpRequest = new SendOtp();
        sendOtpRequest.setEmail(email);

        // Setup VerifyOtp DTO
        verifyOtpRequest = new VerifyOtp();
        verifyOtpRequest.setEmail(email);
        verifyOtpRequest.setOtp(otp);

        when(otpService.generateOtp()).thenReturn(otp);
    }

    // New user - no existing OTP in DB
    @Test
    void sendOtp_NewUser_ShouldSaveAndSendOtp() {
        when(otpRepository.findByEmailId(email)).thenReturn(Optional.empty());

        boolean result = forgotPasswordService.sendOtp(sendOtpRequest);

        assertTrue(result);
        verify(otpRepository, times(1)).save(any(Otp.class));
        verify(emailService, times(1)).sendOtp(eq(email), eq(otp));
    }

    // Existing user - OTP should be updated
    @Test
    void sendOtp_ExistingUser_ShouldUpdateOtp() {
        Otp existingOtp = new Otp();
        existingOtp.setEmailId(email);
        existingOtp.setOtpCode("000000");
        existingOtp.setExpiryTime(LocalDateTime.now().minusMinutes(10));

        when(otpRepository.findByEmailId(email)).thenReturn(Optional.of(existingOtp));

        boolean result = forgotPasswordService.sendOtp(sendOtpRequest);

        assertTrue(result);
        assertEquals(otp, existingOtp.getOtpCode()); // OTP code must be updated
        verify(otpRepository, times(1)).save(existingOtp);
    }

    // DB failure while saving OTP
    @Test
    void sendOtp_DbFailure_ShouldReturnFalse() {
        when(otpRepository.findByEmailId(email)).thenReturn(Optional.empty());
        when(otpRepository.save(any())).thenThrow(new org.springframework.dao.DataAccessException("DB error") {});

        boolean result = forgotPasswordService.sendOtp(sendOtpRequest);

        assertFalse(result);
        verify(emailService, never()).sendOtp(any(), any()); // email should NOT be sent
    }

    // Email/SMTP failure
    @Test
    void sendOtp_EmailFailure_ShouldReturnFalse() {
        when(otpRepository.findByEmailId(email)).thenReturn(Optional.empty());
        doThrow(new org.springframework.mail.MailSendException("SMTP error"))
                .when(emailService).sendOtp(eq(email), eq(otp));

        boolean result = forgotPasswordService.sendOtp(sendOtpRequest);

        assertFalse(result);
    }

    // Valid OTP - should return true and delete OTP
    @Test
    void verifyOtp_ValidOtp_ShouldReturnTrue() {
        Otp otpEntity = new Otp();
        otpEntity.setEmailId(email);
        otpEntity.setOtpCode(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5)); // not expired

        when(otpRepository.findByEmailId(email)).thenReturn(Optional.of(otpEntity));

        boolean result = forgotPasswordService.verifyOtp(verifyOtpRequest);

        assertTrue(result);
        verify(otpRepository, times(1)).delete(otpEntity); // must be deleted after success
    }

    // Wrong OTP - should return false and NOT delete
    @Test
    void verifyOtp_WrongOtp_ShouldReturnFalse() {
        Otp otpEntity = new Otp();
        otpEntity.setEmailId(email);
        otpEntity.setOtpCode("999999");
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        when(otpRepository.findByEmailId(email)).thenReturn(Optional.of(otpEntity));

        verifyOtpRequest.setOtp("123456"); // wrong OTP

        boolean result = forgotPasswordService.verifyOtp(verifyOtpRequest);

        assertFalse(result);
        verify(otpRepository, never()).delete(any()); // must NOT delete on wrong OTP
    }

    // Expired OTP - should return false
    @Test
    void verifyOtp_ExpiredOtp_ShouldReturnFalse() {
        Otp otpEntity = new Otp();
        otpEntity.setEmailId(email);
        otpEntity.setOtpCode(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().minusMinutes(10)); // already expired

        when(otpRepository.findByEmailId(email)).thenReturn(Optional.of(otpEntity));

        boolean result = forgotPasswordService.verifyOtp(verifyOtpRequest);

        assertFalse(result);
        verify(otpRepository, never()).delete(any());
    }

    // No OTP found in DB
    @Test
    void verifyOtp_NoOtpFound_ShouldReturnFalse() {
        when(otpRepository.findByEmailId(email)).thenReturn(Optional.empty());

        boolean result = forgotPasswordService.verifyOtp(verifyOtpRequest);

        assertFalse(result);
    }
}