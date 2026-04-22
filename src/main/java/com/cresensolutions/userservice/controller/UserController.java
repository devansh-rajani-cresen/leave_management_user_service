// In this UserController file, logic related to Login and forgot password service implemented

package com.cresensolutions.userservice.controller;

import com.cresensolutions.userservice.dto.*;
import com.cresensolutions.userservice.repository.UserRepository;
import com.cresensolutions.userservice.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    public UserController(UserService userService, UserRepository userRepository,
                          ForgotPasswordService forgotPasswordService,
                          ResetPasswordService resetPasswordService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.forgotPasswordService = forgotPasswordService;
        this.resetPasswordService = resetPasswordService;
    }

    // LOGIN SECTION

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    // FORGOT PASSWORD SECTION

    @PostMapping("/send-otp")
    public ResponseEntity<SuccessResponse> sendOtp(@RequestBody SendOtp sendOtpRequest) {
        log.info("Sending OTP for Email: {}", sendOtpRequest.getEmail());
        forgotPasswordService.sendOtp(sendOtpRequest);
        return ResponseEntity.ok(
                new SuccessResponse("OTP sent successfully")
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<SuccessResponse> verifyOtp(@RequestBody VerifyOtp verifyOtpRequest) {
        forgotPasswordService.verifyOtp(verifyOtpRequest);
        return ResponseEntity.ok(
                new SuccessResponse("OTP verified successfully")
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<SuccessResponse> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        resetPasswordService.resetPassword(resetPasswordRequest);
        return ResponseEntity.ok(
                new SuccessResponse("Password reset successfully")
        );
    }

    // RETURN MANAGERS

    @GetMapping("/get-managers")  // send managers to frontend to display in dropdown
    public List<ManagerResponse> getManagers() {
        log.info("Fetching all managers!");
        return userService.getManagers();
    }

}