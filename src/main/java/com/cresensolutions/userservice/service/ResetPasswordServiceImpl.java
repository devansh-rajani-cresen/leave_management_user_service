package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.ResetPasswordRequest;
import com.cresensolutions.userservice.entity.User;
import com.cresensolutions.userservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ResetPasswordServiceImpl implements ResetPasswordService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ResetPasswordServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String reqUserMail = resetPasswordRequest.getEmail();
        String reqUserPassword = resetPasswordRequest.getNewPassword();

        Optional<User> dbUserMail = userRepository.findByUserName(reqUserMail);

        if (dbUserMail.isEmpty()) {
            return false;
        }

        try {
            User user = dbUserMail.get();
            user.setUserPswd(passwordEncoder.encode(reqUserPassword)); // encode and set new password
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
