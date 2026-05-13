package com.cresensolutions.userservice.service.impl;

import com.cresensolutions.userservice.dto.DeleteUser;
import com.cresensolutions.userservice.dto.UserRequest;
import com.cresensolutions.userservice.dto.UserResponse;
import com.cresensolutions.userservice.entity.Role;
import com.cresensolutions.userservice.entity.User;
import com.cresensolutions.userservice.exception.CustomException;
import com.cresensolutions.userservice.repository.RoleRepository;
import com.cresensolutions.userservice.repository.UserRepository;
import com.cresensolutions.userservice.service.AdminService;
import com.cresensolutions.userservice.service.EmailService;
import com.cresensolutions.userservice.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RestTemplate restTemplate;

    @Value("${create.leave.url}")
    private String createLeaveUrl;

    // Fetch & Display all Users from DB
    @Override
    public List<UserResponse> getUsers() {
        List<User> users = userRepository.findAll();

        return users
                .stream()
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setId(user.getId());
                    response.setUserName(user.getUserName());
                    response.setFullName(user.getFullName());
                    response.setEmailId(user.getEmailId());
                    Role role = user.getRole();
                    response.setRoleId(role != null ? role.getId() : null);
                    response.setRole(role != null ? role.getRoleName() : user.getRoleName());
                    response.setGender(user.getGender());
                    response.setActive(user.getActive());
                    response.setCompanyId(user.getCompanyId());
                    return response;
                }).toList();
    }

    // Save User to DB (Add Employee)
    @Override
    @Transactional
    public void saveUser(UserRequest userRequest) {
        try {
            User user = buildUser(userRequest);
            User savedUser = userRepository.save(user);
            createEmployeeLeave(savedUser);
            sendWelcomeEmail(userRequest, user);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error saving user", e);
            throw new CustomException("Unable to create user. Please try again.", 500);
        }
    }

    // Delete User by Username (Delete Employee)
    @Override
    @Transactional
    public void deleteUser(DeleteUser deleteUser) {
        String fullName = deleteUser.getFullName();
        String userName = deleteUser.getUserName();
        String emailId = deleteUser.getEmailId();
        boolean exists = userRepository.existsByUserName(userName);
        if (!exists) {
            throw new CustomException("User not found", 404);
        }
        try {
            userRepository.deleteByUserName(userName);
            emailService.sendDeleteMessage(fullName, emailId);
        } catch (Exception e) {
            throw new CustomException("Failed to delete user", 500);
        }
    }

    // Update user
    @Override
    public void updateUser(UserRequest userRequest) {
        String userName = userRequest.getUserName();
        validateUsername(userName);
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new CustomException("User not found!", 404));

        try {
            Map<String, String> updatedFields = new LinkedHashMap<>();
            captureBasicDetailChanges(userRequest, user, updatedFields);
            updateBasicDetails(userRequest, user);
            updateRole(userRequest, user, updatedFields);
            updatePassword(userRequest, user, updatedFields);
            user.setUpdateDate(DateTimeUtil.nowInIst());
            user.setUpdatedBy(userRequest.getCreatedBy());
            userRepository.save(user);
            sendUpdateEmailIfNeeded(user, updatedFields);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("Failed to update user!", 500);
        }
    }

    // HELPER: for Create Employee

    private User buildUser(UserRequest userRequest){

        Role role = resolveRole(userRequest);
        User user = new User();

        user.setFullName(userRequest.getFullName());
        user.setUserName(userRequest.getUserName());
        user.setUserPswd(passwordEncoder.encode(userRequest.getUserPassword()));
        user.setEmailId(userRequest.getEmailId());
        user.setRole(role);
        user.setRoleName(role.getRoleName());
        user.setGender(userRequest.getGender());
        user.setActive(userRequest.getActive());
        user.setCompanyId(userRequest.getCompanyId());
        user.setCreatedBy(userRequest.getCreatedBy());
        user.setCreateDate(DateTimeUtil.nowInIst());

        return user;
    }

    private void createEmployeeLeave(User savedUser){

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();

            payload.put("userId", savedUser.getId());
            payload.put("fullName", savedUser.getFullName());
            payload.put("emailId", savedUser.getEmailId());
            payload.put("gender", savedUser.getGender());

            HttpEntity<Map<String, Object>> requestEntity =
                    new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(
                    createLeaveUrl,
                    requestEntity,
                    String.class
            );

        } catch (Exception e) {
            throw new CustomException("Failed to create employee leave", 500);
        }
    }

    private void sendWelcomeEmail(UserRequest userRequest, User user){
        emailService.sendWelcomeMessage(
                userRequest.getEmailId(),
                user.getUserName(),
                userRequest.getUserPassword()
        );
    }

    private Role resolveRole(UserRequest userRequest) {
        if (userRequest.getRoleId() != null) {
            return roleRepository.findById(userRequest.getRoleId())
                    .orElseThrow(() -> new CustomException("Role not found for id: " + userRequest.getRoleId(), 404));
        }

        if (userRequest.getRole() != null && !userRequest.getRole().isBlank()) {
            return roleRepository.findByRoleNameIgnoreCase(userRequest.getRole().trim())
                    .orElseThrow(() -> new CustomException("Role not found for name: " + userRequest.getRole(), 404));
        }

        throw new CustomException("Role is required", 400);
    }

    // HELPER: for Update Employee

    private void validateUsername(String userName){
        if(userName == null || userName.isBlank()){
            throw new CustomException("Username is required", 400);
        }
    }

    private void updateBasicDetails(UserRequest request, User user){
        if(request.getFullName() != null &&
                !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if(request.getEmailId() != null &&
                !request.getEmailId().isBlank()) {
            user.setEmailId(request.getEmailId());
        }

        if(request.getGender() != null &&
                !request.getGender().isBlank()) {
            user.setGender(request.getGender());
        }

        if(request.getCompanyId() != null &&
                !request.getCompanyId().isBlank()) {
            user.setCompanyId(request.getCompanyId());
        }

        if(request.getActive() != null){
            user.setActive(request.getActive());
        }
    }

    private void captureBasicDetailChanges(UserRequest request, User user, Map<String, String> updatedFields) {
        if (hasChanged(request.getFullName(), user.getFullName())) {
            updatedFields.put("Full Name", request.getFullName().trim());
        }

        if (hasChanged(request.getEmailId(), user.getEmailId())) {
            updatedFields.put("Email", request.getEmailId().trim());
        }

        if (hasChanged(request.getGender(), user.getGender())) {
            updatedFields.put("Gender", request.getGender().trim());
        }

        if (hasChanged(request.getCompanyId(), user.getCompanyId())) {
            updatedFields.put("Company ID", request.getCompanyId().trim());
        }

        if (request.getActive() != null && !request.getActive().equals(user.getActive())) {
            updatedFields.put("Account Status", Boolean.TRUE.equals(request.getActive()) ? "Active" : "Inactive");
        }
    }

    private void updateRole(UserRequest request, User user, Map<String, String> updatedFields){
        if(request.getRoleId() != null ||
                (request.getRole() != null &&
                        !request.getRole().isBlank())) {

            Role role = resolveRole(request);
            if (user.getRole() == null || !role.getId().equals(user.getRole().getId())) {
                updatedFields.put("Role", role.getRoleName());
            }

            user.setRole(role);
            user.setRoleName(role.getRoleName());
        }
    }

    private void updatePassword(UserRequest request, User user, Map<String, String> updatedFields){
        if(request.getUserPassword() != null && !request.getUserPassword().isBlank()) {
            user.setUserPswd(passwordEncoder.encode(request.getUserPassword()));
            updatedFields.put("Password", request.getUserPassword());
        }
    }

    private void sendUpdateEmailIfNeeded(User user, Map<String, String> updatedFields) {
        if (!updatedFields.isEmpty()) {
            emailService.sendUpdateMessage(user.getEmailId(), user.getUserName(), updatedFields);
        }
    }

    private boolean hasChanged(String requestedValue, String existingValue) {
        return requestedValue != null
                && !requestedValue.isBlank()
                && !requestedValue.trim().equals(existingValue);
    }
}
