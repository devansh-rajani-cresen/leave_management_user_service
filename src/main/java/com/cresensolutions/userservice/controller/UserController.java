package com.cresensolutions.userservice.controller;

import com.cresensolutions.userservice.dto.*;
import com.cresensolutions.userservice.repository.UserRepository;
import com.cresensolutions.userservice.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class UserController {

    public final UserService userService;
    public final UserRepository userRepository;
    private final ForgotPasswordService forgotPasswordService;
    private final ResetPasswordService resetPasswordService;

    public UserController(UserService userService, UserRepository userRepository, ForgotPasswordService forgotPasswordService, ResetPasswordService resetPasswordService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.forgotPasswordService = forgotPasswordService;
        this.resetPasswordService = resetPasswordService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/send-otp")
    public boolean sendOtp(@RequestBody SendOtp sendOtpRequest) {
        log.info("Sending OTP for Email: {}",sendOtpRequest.getEmail());
        return forgotPasswordService.sendOtp(sendOtpRequest);
    }

    @PostMapping("/verify-otp")
    public boolean verifyOtp(@RequestBody VerifyOtp verifyOtpRequest) {
        return forgotPasswordService.verifyOtp(verifyOtpRequest);
    }

    @PostMapping("/reset-password")
    public boolean resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        return resetPasswordService.resetPassword(resetPasswordRequest);
    }
}
