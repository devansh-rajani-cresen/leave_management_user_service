package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.ResetPasswordRequest;
import com.cresensolutions.userservice.entity.User;
import com.cresensolutions.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class ResetPassword {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ResetPassword(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String reqUserMail = resetPasswordRequest.getEmail();
        String reqUserPassword = resetPasswordRequest.getNewPassword();

        Optional<User> dbUserMail = userRepository.findByUserName(reqUserMail);

        if (dbUserMail.isEmpty()) {
            log.warn("User not found for email: {}", reqUserMail);
            return false;
        }

        try {
            User user = dbUserMail.get();
            user.setUserPswd(passwordEncoder.encode(reqUserPassword)); // encode and set new password
            userRepository.save(user);
            log.info("Password reset successfully for: {}", reqUserMail);
            return true;
        } catch (Exception e) {
            log.warn("Error resetting password for: {} and Error is: {}", reqUserMail, e.getMessage());
            return false;
        }
    }
}