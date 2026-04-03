package com.cresensolutions.userservice.service;
import com.cresensolutions.userservice.dto.ResetPasswordRequest;

public interface ResetPasswordService {
    boolean resetPassword(ResetPasswordRequest resetPasswordRequest);
}