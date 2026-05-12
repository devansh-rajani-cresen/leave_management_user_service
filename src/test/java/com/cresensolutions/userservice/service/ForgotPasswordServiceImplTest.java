package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.SendOtp;
import com.cresensolutions.userservice.dto.VerifyOtp;
import com.cresensolutions.userservice.entity.Otp;
import com.cresensolutions.userservice.exception.CustomException;
import com.cresensolutions.userservice.repository.OtpRepository;
import com.cresensolutions.userservice.service.impl.ForgotPasswordServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailException;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordServiceImplTest {

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @Mock
    private OtpRepository otpRepository;

    @InjectMocks
    private ForgotPasswordServiceImpl service;

    // --- SEND OTP TESTS ---

    @Test
    void sendOtp_newUser_success() {
        SendOtp req = new SendOtp();
        req.setEmail("new@mail.com");

        when(otpService.generateOtp()).thenReturn("123456");
        when(otpRepository.findByEmailId("new@mail.com")).thenReturn(Optional.empty());

        service.sendOtp(req);

        // Verifies the "else" branch where a new entity is created
        verify(otpRepository, times(1)).save(any(Otp.class));
        verify(emailService, times(1)).sendOtp("new@mail.com", "123456");
    }

    @Test
    void sendOtp_existingUser_success() {
        SendOtp req = new SendOtp();
        req.setEmail("existing@mail.com");

        Otp existingOtp = new Otp();
        existingOtp.setEmailId("existing@mail.com");

        when(otpService.generateOtp()).thenReturn("123456");
        when(otpRepository.findByEmailId("existing@mail.com")).thenReturn(Optional.of(existingOtp));

        service.sendOtp(req);

        assertEquals("123456", existingOtp.getOtpCode());
        verify(otpRepository).save(existingOtp);
    }

    @Test
    void sendOtp_databaseException_throwsCustomException() {
        SendOtp req = new SendOtp();
        req.setEmail("db@fail.com");

        when(otpService.generateOtp()).thenReturn("111111");
        when(otpRepository.findByEmailId(anyString())).thenReturn(Optional.empty());
        // Triggering the DataAccessException catch block
        doThrow(new DataAccessException("Disk Full") {}).when(otpRepository).save(any());

        CustomException ex = assertThrows(CustomException.class, () -> service.sendOtp(req));
        assertEquals("Database error while saving OTP", ex.getMessage());
        assertEquals(500, ex.getStatus());
    }

    @Test
    void sendOtp_mailException_throwsCustomException() {
        SendOtp req = new SendOtp();
        req.setEmail("mail@fail.com");

        when(otpService.generateOtp()).thenReturn("222222");
        when(otpRepository.findByEmailId(anyString())).thenReturn(Optional.empty());
        // Triggering the MailException catch block
        doThrow(new MailException("SMTP Down") {}).when(emailService).sendOtp(anyString(), anyString());

        CustomException ex = assertThrows(CustomException.class, () -> service.sendOtp(req));
        assertEquals("Failed to send OTP email", ex.getMessage());
    }

    // --- VERIFY OTP TESTS ---

    @Test
    void verifyOtp_success() {
        VerifyOtp req = new VerifyOtp();
        req.setEmail("verify@mail.com");
        req.setOtp("999999");

        Otp otp = new Otp();
        otp.setEmailId("verify@mail.com");
        otp.setOtpCode("999999");
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        when(otpRepository.findByEmailId("verify@mail.com")).thenReturn(Optional.of(otp));

        assertDoesNotThrow(() -> service.verifyOtp(req));
        verify(otpRepository, times(1)).delete(otp);
    }

    @Test
    void verifyOtp_emailNotFound_throws404() {
        VerifyOtp req = new VerifyOtp();
        req.setEmail("none@mail.com");

        when(otpRepository.findByEmailId("none@mail.com")).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class, () -> service.verifyOtp(req));
        assertEquals("OTP not found for this email", ex.getMessage());
        assertEquals(404, ex.getStatus());
    }

    @Test
    void verifyOtp_expired_throws400() {
        VerifyOtp req = new VerifyOtp();
        req.setEmail("expired@mail.com");
        req.setOtp("123456");

        Otp otp = new Otp();
        otp.setOtpCode("123456");
        // Setting time to the past
        otp.setExpiryTime(LocalDateTime.now().minusSeconds(1));

        when(otpRepository.findByEmailId("expired@mail.com")).thenReturn(Optional.of(otp));

        CustomException ex = assertThrows(CustomException.class, () -> service.verifyOtp(req));
        assertEquals("OTP has expired", ex.getMessage());
        assertEquals(400, ex.getStatus());
    }

    @Test
    void verifyOtp_invalidOtpCode_throws400() {
        VerifyOtp req = new VerifyOtp();
        req.setEmail("user@mail.com");
        req.setOtp("wrong_otp");

        Otp otp = new Otp();
        otp.setOtpCode("correct_otp");
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        when(otpRepository.findByEmailId("user@mail.com")).thenReturn(Optional.of(otp));

        CustomException ex = assertThrows(CustomException.class, () -> service.verifyOtp(req));
        assertEquals("Invalid OTP", ex.getMessage());
        assertEquals(400, ex.getStatus());
    }
}
