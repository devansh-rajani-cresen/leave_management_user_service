package com.cresensolutions.userservice.service;

import java.util.Map;

public interface EmailService {
    // for sending OTP on forgot password
    void sendOtp(String toEmail, String otp);

    // for sending welcoming message on creating new employee
    void sendWelcomeMessage(String toEmail, String username, String password);

    // send mail on updating user
    void sendUpdateMessage(String toEmail, String username, Map<String, String> updatedFields);

    // send mail on deleting user
    void sendDeleteMessage(String fullName, String toEmail);
}
