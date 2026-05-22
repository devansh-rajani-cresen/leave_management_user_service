package com.cresensolutions.userservice.service;
import com.cresensolutions.userservice.dto.DeleteUser;
import com.cresensolutions.userservice.dto.UserRequest;
import com.cresensolutions.userservice.dto.UserResponse;
import java.util.List;

public interface AdminService {
    List<UserResponse> getUsers();
    void saveUser(UserRequest userRequest);
    void deleteUser(DeleteUser deleteUser);
    void updateUser(UserRequest userRequest);
}
