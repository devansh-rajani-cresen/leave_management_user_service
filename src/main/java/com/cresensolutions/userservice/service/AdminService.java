package com.cresensolutions.userservice.service;
import com.cresensolutions.userservice.dto.UserRequest;
import com.cresensolutions.userservice.dto.UserResponse;
import java.util.List;

public interface AdminService {
    List<UserResponse> getUsers();
    boolean saveUser(UserRequest userRequest);
    boolean deleteUserByUsername(String username);
    boolean updateUser(UserRequest userRequest);
}
