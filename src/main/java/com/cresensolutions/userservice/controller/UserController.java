package com.cresensolutions.userservice.controller;

import com.cresensolutions.userservice.dto.*;
import com.cresensolutions.userservice.repository.UserRepository;
import com.cresensolutions.userservice.service.ForgotPasswordService;
import com.cresensolutions.userservice.service.ResetPassword;
import com.cresensolutions.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class UserController {

    public final UserService userService;
    public final UserRepository userRepository;
    private final ForgotPasswordService forgotPasswordService;
    private final ResetPassword resetPassword;

    public UserController(UserService userService, UserRepository userRepository, ForgotPasswordService forgotPasswordService, ResetPassword resetPassword) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.forgotPasswordService = forgotPasswordService;
        this.resetPassword = resetPassword;
    }

    @GetMapping("/test")
    public String test(){
        long count = userRepository.count();
        return "Total users: "+count;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request){
        log.info("Request received: {}", request);
        return userService.login(request);
    }

    @PostMapping("/send-otp")
    public boolean sendOtp(@RequestBody SendOtp sendOtpRequest){
        return forgotPasswordService.sendOtp(sendOtpRequest);
    }

    @PostMapping("/verify-otp")
    public boolean verifyOtp(@RequestBody VerifyOtp verifyOtpRequest){
        return forgotPasswordService.verifyOtp(verifyOtpRequest);
    }

    @PostMapping("/reset-password")
    public boolean resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest){
        log.info("Payload from frontend for Reset Password : {}", resetPasswordRequest);
        return resetPassword.resetPassword(resetPasswordRequest);
    }
}
