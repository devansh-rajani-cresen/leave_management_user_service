package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.SendOtp;
import com.cresensolutions.userservice.dto.VerifyOtp;
import com.cresensolutions.userservice.entity.Otp;
import com.cresensolutions.userservice.repository.OtpRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class ForgotPasswordService {

    private final OtpService otpService;
    private final EmailService emailService;
    private final OtpRepository otpRepository;

    public ForgotPasswordService(OtpService otpService, EmailService emailService, OtpRepository otpRepository) {
        this.otpService = otpService;
        this.emailService = emailService;
        this.otpRepository = otpRepository;
    }

    public boolean sendOtp(SendOtp sendOtpRequest) {

        String email = sendOtpRequest.getEmail();
        log.info("Send OTP request for Email : {}", email);

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
            return true;
        } catch (DataAccessException e) {
            log.warn("DB error while saving OTP for : {} and getting Error : {}", email, e.getMessage());
            return false;
        } catch (MailException e) {
            log.warn("Email send failed for : {} and getting Error : {}", email, e.getMessage());
            return false;
        }
    }

    public boolean verifyOtp(VerifyOtp verifyOtpRequest) {

        String email = verifyOtpRequest.getEmail();
        String inputOtp = verifyOtpRequest.getOtp();

        Optional<Otp> dbOtp = otpRepository.findByEmailId(email);

        if (dbOtp.isEmpty()) {
            log.warn("No OTP found for email : {}", email);
            return false;
        }

        Otp otpEntity = dbOtp.get();

        // Check expiry
        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            log.warn("OTP expired for email : {}", email);
            return false;
        }

        // Match OTP
        boolean isMatched = otpEntity.getOtpCode().equals(inputOtp);

        if (isMatched) {
            otpRepository.delete(otpEntity); // delete OTP after successful verification
            log.info("OTP verified and deleted for email : {}", email);
        } else {
            log.warn("OTP mismatch for email : {} ", email);
        }

        return isMatched;
    }
}