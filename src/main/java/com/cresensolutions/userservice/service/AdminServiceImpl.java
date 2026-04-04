package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.DeleteUser;
import com.cresensolutions.userservice.dto.UserRequest;
import com.cresensolutions.userservice.dto.UserResponse;
import com.cresensolutions.userservice.entity.User;
import com.cresensolutions.userservice.exception.CustomException;
import com.cresensolutions.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AdminServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // Fetch & Display all Users from DB
    @Override
    public List<UserResponse> getUsers() {
        List<User> users = userRepository.findAll();
        log.info("Total users fetched: {}", users.size());

        return users.stream().map(user -> {
            UserResponse response = new UserResponse();
            response.setId(user.getId());
            response.setUserName(user.getUserName());
            response.setFullName(user.getFullName());
            response.setEmailId(user.getEmailId());
            response.setRole(user.getRole());
            response.setGender(user.getGender());
            response.setActive(user.getActive());
            response.setCompanyId(user.getCompanyId());
            return response;
        }).toList();
    }

    // Save User to DB (Add Employee)
    @Override
    public void saveUser(UserRequest userRequest) {
        try {
            User user = new User();
            user.setFullName(userRequest.getFullName());
            user.setUserName(userRequest.getUserName());
            user.setUserPswd(passwordEncoder.encode(userRequest.getUserPassword()));
            user.setEmailId(userRequest.getEmailId());
            user.setRole(userRequest.getRole());
            user.setGender(userRequest.getGender());
            user.setActive(userRequest.getActive());
            user.setCompanyId(userRequest.getCompanyId());
            user.setCreatedBy(userRequest.getCreatedBy());
            user.setCreateDate(OffsetDateTime.now());

            userRepository.save(user);
            emailService.sendWelcomeMessage(
                    userRequest.getEmailId(),
                    user.getUserName(),
                    userRequest.getUserPassword()
            );
            log.info("User saved successfully: {}", userRequest.getUserName());
        } catch (Exception e) {
            log.error("Error saving user: {}. Error: {}", userRequest.getUserName(), e.getMessage());
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
            log.info("First deleting user with username from DB: {}", userName);
            userRepository.deleteByUserName(userName);
            log.info("Now sending delete mail to that user: {}", fullName);
            emailService.sendDeleteMessage(
                    fullName,
                    emailId
            );
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage());
            throw new CustomException("Failed to delete user", 500);
        }
    }

    // Update user
    @Override
    public void updateUser(UserRequest userRequest) {

        String userName = userRequest.getUserName();

        if (userName == null || userName.isBlank()) {
            throw new CustomException("Username is required", 400);
        }

        Optional<User> existingUser = userRepository.findByUserName(userName);

        if (existingUser.isEmpty()) {
            throw new CustomException("User not found", 404);
        }

        try {
            User user = existingUser.get();

            if (userRequest.getFullName() != null && !userRequest.getFullName().isBlank()) {
                user.setFullName(userRequest.getFullName());
            }
            if (userRequest.getEmailId() != null && !userRequest.getEmailId().isBlank()) {
                user.setEmailId(userRequest.getEmailId());
            }
            if (userRequest.getRole() != null && !userRequest.getRole().isBlank()) {
                user.setRole(userRequest.getRole());
            }
            if (userRequest.getGender() != null && !userRequest.getGender().isBlank()) {
                user.setGender(userRequest.getGender());
            }
            if (userRequest.getCompanyId() != null && !userRequest.getCompanyId().isBlank()) {
                user.setCompanyId(userRequest.getCompanyId());
            }
            if (userRequest.getActive() != null) {
                user.setActive(userRequest.getActive());
            }
            if (userRequest.getUserPassword() != null && !userRequest.getUserPassword().isBlank()) {
                user.setUserPswd(passwordEncoder.encode(userRequest.getUserPassword()));
            }

            user.setUpdateDate(OffsetDateTime.now());
            user.setUpdatedBy(userRequest.getCreatedBy());

            userRepository.save(user);

        } catch (Exception e) {
            log.error("Error updating user: {}. Error: {}", userName, e.getMessage());
            throw new CustomException("Failed to update user", 500);
        }
    }
}
