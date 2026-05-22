// In this file, logic related to Login, forgot password, OTP and Reset Password implemented

package com.cresensolutions.userservice.controller;

import com.cresensolutions.userservice.dto.*;
import com.cresensolutions.userservice.service.*;
import com.cresensolutions.userservice.util.JwtRequestUtil;
import com.cresensolutions.userservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ForgotPasswordService forgotPasswordService;
    private final ResetPasswordService resetPasswordService;
    private final MyProfileService myProfileService;
    private final JwtRequestUtil jwtRequestUtil;
    private final JwtUtil jwtUtil;

    // LOGIN SECTION
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    // FORGOT PASSWORD SECTION
    @PostMapping("/send-otp")
    public ResponseEntity<SuccessResponse> sendOtp(@RequestBody SendOtp sendOtpRequest) {
        forgotPasswordService.sendOtp(sendOtpRequest);
        return ResponseEntity.ok(
                new SuccessResponse("OTP sent!")
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<SuccessResponse> verifyOtp(@RequestBody VerifyOtp verifyOtpRequest) {
        forgotPasswordService.verifyOtp(verifyOtpRequest);
        return ResponseEntity.ok(
                new SuccessResponse("OTP verified!")
        );
    }

    // RESET PASSWORD SECTION
    @PostMapping("/reset-password")
    public ResponseEntity<SuccessResponse> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        resetPasswordService.resetPassword(resetPasswordRequest);
        return ResponseEntity.ok(
                new SuccessResponse("Password reset!")
        );
    }

    // RETURN MANAGERS TO FRONTEND
    @GetMapping("/get-managers")
    public List<ManagerResponse> getManagers() {
        return userService.getManagers();
    }

    // USER PROFILE
    @GetMapping("/my-profile")
    public MyProfile getMyProfile(HttpServletRequest request){
        String token = jwtRequestUtil.extractToken(request);
        Long userId = jwtUtil.extractUserId(token);
        return myProfileService.getMyProfileData(userId);
    }

}