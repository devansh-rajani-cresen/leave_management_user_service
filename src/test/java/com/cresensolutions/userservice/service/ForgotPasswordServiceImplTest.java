package com.cresensolutions.userservice.service;//package com.cresensolutions.userservice.service;
//
//import com.cresensolutions.userservice.dto.SendOtp;
//import com.cresensolutions.userservice.dto.VerifyOtp;
//import com.cresensolutions.userservice.entity.Otp;
//import com.cresensolutions.userservice.repository.OtpRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import java.time.LocalDateTime;
//import java.util.Optional;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//import org.mockito.quality.Strictness;

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

    @Mock private OtpService otpService;
    @Mock private EmailService emailService;
    @Mock private OtpRepository otpRepository;

    @InjectMocks
    private ForgotPasswordServiceImpl service;

    // ---------- SEND OTP ----------

    @Test
    void sendOtp_newUser_success() {
        SendOtp req = new SendOtp();
        req.setEmail("test@mail.com");

        when(otpService.generateOtp()).thenReturn("123456");
        when(otpRepository.findByEmailId("test@mail.com")).thenReturn(Optional.empty());

        service.sendOtp(req);

        verify(otpRepository).save(any());
        verify(emailService).sendOtp("test@mail.com", "123456");
    }

    @Test
    void sendOtp_existingUser_success() {
        SendOtp req = new SendOtp();
        req.setEmail("test@mail.com");

        Otp otp = new Otp();
        otp.setEmailId("test@mail.com");

        when(otpService.generateOtp()).thenReturn("123456");
        when(otpRepository.findByEmailId("test@mail.com")).thenReturn(Optional.of(otp));

        service.sendOtp(req);

        verify(otpRepository).save(otp);
        verify(emailService).sendOtp("test@mail.com", "123456");
    }

    @Test
    void sendOtp_databaseException() {
        SendOtp req = new SendOtp();
        req.setEmail("test@mail.com");

        when(otpService.generateOtp()).thenReturn("123456");
        when(otpRepository.findByEmailId("test@mail.com")).thenReturn(Optional.empty());
        when(otpRepository.save(any())).thenThrow(new DataAccessException("db error") {});

        CustomException ex = assertThrows(CustomException.class,
                () -> service.sendOtp(req));

        assertEquals("Database error while saving OTP", ex.getMessage());
    }

    @Test
    void sendOtp_mailException() {
        SendOtp req = new SendOtp();
        req.setEmail("test@mail.com");

        when(otpService.generateOtp()).thenReturn("123456");
        when(otpRepository.findByEmailId("test@mail.com")).thenReturn(Optional.empty());
        doThrow(new MailException("mail error") {})
                .when(emailService).sendOtp(any(), any());

        CustomException ex = assertThrows(CustomException.class,
                () -> service.sendOtp(req));

        assertEquals("Failed to send OTP email", ex.getMessage());
    }

    // ---------- VERIFY OTP ----------

    @Test
    void verifyOtp_success() {
        VerifyOtp req = new VerifyOtp();
        req.setEmail("test@mail.com");
        req.setOtp("123456");

        Otp otp = new Otp();
        otp.setEmailId("test@mail.com");
        otp.setOtpCode("123456");
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        when(otpRepository.findByEmailId("test@mail.com")).thenReturn(Optional.of(otp));

        service.verifyOtp(req);

        verify(otpRepository).delete(otp);
    }

    @Test
    void verifyOtp_notFound() {
        VerifyOtp req = new VerifyOtp();
        req.setEmail("test@mail.com");

        when(otpRepository.findByEmailId("test@mail.com")).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> service.verifyOtp(req));
    }

    @Test
    void verifyOtp_expired() {
        VerifyOtp req = new VerifyOtp();
        req.setEmail("test@mail.com");
        req.setOtp("123456");

        Otp otp = new Otp();
        otp.setOtpCode("123456");
        otp.setExpiryTime(LocalDateTime.now().minusMinutes(1));

        when(otpRepository.findByEmailId("test@mail.com")).thenReturn(Optional.of(otp));

        assertThrows(CustomException.class, () -> service.verifyOtp(req));
    }

    @Test
    void verifyOtp_invalidOtp() {
        VerifyOtp req = new VerifyOtp();
        req.setEmail("test@mail.com");
        req.setOtp("wrong");

        Otp otp = new Otp();
        otp.setOtpCode("123456");
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        when(otpRepository.findByEmailId("test@mail.com")).thenReturn(Optional.of(otp));

        assertThrows(CustomException.class, () -> service.verifyOtp(req));
    }
}