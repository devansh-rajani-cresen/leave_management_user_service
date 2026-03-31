package com.cresensolutions.userservice.service;
import com.cresensolutions.userservice.dto.LoginRequest;
import com.cresensolutions.userservice.dto.LoginResponse;

public interface UserService {
    LoginResponse login(LoginRequest request);
}
