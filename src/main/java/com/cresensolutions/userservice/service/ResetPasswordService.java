package com.cresensolutions.userservice.service;
import com.cresensolutions.userservice.dto.ResetPasswordRequest;

public interface ResetPasswordService {
    void resetPassword(ResetPasswordRequest resetPasswordRequest);
}