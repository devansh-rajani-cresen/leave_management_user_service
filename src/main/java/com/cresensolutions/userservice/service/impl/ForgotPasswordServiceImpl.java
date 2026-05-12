package com.cresensolutions.userservice.service.impl;

import com.cresensolutions.userservice.dto.SendOtp;
import com.cresensolutions.userservice.dto.VerifyOtp;
import com.cresensolutions.userservice.entity.Otp;
import com.cresensolutions.userservice.exception.CustomException;
import com.cresensolutions.userservice.repository.OtpRepository;
import com.cresensolutions.userservice.service.EmailService;
import com.cresensolutions.userservice.service.ForgotPasswordService;
import com.cresensolutions.userservice.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final OtpService otpService;
    private final EmailService emailService;
    private final OtpRepository otpRepository;

    @Override
    public void sendOtp(SendOtp sendOtpRequest) {
        String email = sendOtpRequest.getEmail();

        String otp = otpService.generateOtp();
        Optional<Otp> existing = otpRepository.findByEmailId(email);
        Otp otpEntity;

        if (existing.isPresent()) {
            otpEntity = existing.get();
        } else {
            otpEntity = new Otp();
            otpEntity.setEmailId(email);
        }

        otpEntity.setOtpCode(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        try {
            otpRepository.save(otpEntity);
            emailService.sendOtp(email, otp);
        } catch (DataAccessException e) {
            throw new CustomException("Database error while saving OTP", 500);
        } catch (MailException e) {
            throw new CustomException("Failed to send OTP email", 500);
        }
    }

    @Override
    public void verifyOtp(VerifyOtp verifyOtpRequest) {

        String email = verifyOtpRequest.getEmail();
        String inputOtp = verifyOtpRequest.getOtp();

        Optional<Otp> dbOtp = otpRepository.findByEmailId(email);

        if (dbOtp.isEmpty()) {
            throw new CustomException("OTP not found for this email", 404);
        }

        Otp otpEntity = dbOtp.get();

        // Check expiry
        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new CustomException("OTP has expired", 400);
        }
        // Match OTP
        if (!otpEntity.getOtpCode().equals(inputOtp)) {
            throw new CustomException("Invalid OTP", 400);
        }
        // Success case
        otpRepository.delete(otpEntity);
    }
}
