package com.cresensolutions.userservice.service.impl;

import com.cresensolutions.userservice.dto.ResetPasswordRequest;
import com.cresensolutions.userservice.entity.User;
import com.cresensolutions.userservice.exception.CustomException;
import com.cresensolutions.userservice.repository.UserRepository;
import com.cresensolutions.userservice.service.ResetPasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResetPasswordServiceImpl implements ResetPasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String reqUserMail = resetPasswordRequest.getEmail();
        String reqUserPassword = resetPasswordRequest.getNewPassword();

        Optional<User> dbUserMail = userRepository.findByEmailId(reqUserMail);

        if (dbUserMail.isEmpty()) {
            throw new CustomException("User not found with email!", 404);
        }

        try {
            User user = dbUserMail.get();
            // New password can not same as old password
            if(passwordEncoder.matches(reqUserPassword, user.getUserPswd())){
                throw new CustomException("New password cannot be same as old password!", 400);
            }
            user.setUserPswd(passwordEncoder.encode(reqUserPassword));
            userRepository.save(user);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("Failed to reset password!", 500);
        }
    }

}
