package com.cresensolutions.userservice.service;
import com.cresensolutions.userservice.dto.BasicUserInfoForAI;
import com.cresensolutions.userservice.dto.LoginRequest;
import com.cresensolutions.userservice.dto.LoginResponse;
import com.cresensolutions.userservice.dto.ManagerResponse;
import java.util.List;

public interface UserService {
    LoginResponse login(LoginRequest request);
    List<ManagerResponse> getManagers();
    List<BasicUserInfoForAI> searchUserByName(String name);
    List<BasicUserInfoForAI> getUserInfoByRole(String role);
    long getEmployeeCount();
}
