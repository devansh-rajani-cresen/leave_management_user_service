package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.UserRequest;
import com.cresensolutions.userservice.dto.UserResponse;
import com.cresensolutions.userservice.entity.User;
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
    public boolean saveUser(UserRequest userRequest) {
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

            // save user into DB & Send Welcome Message
            userRepository.save(user);
            emailService.sendWelcomeMessage(userRequest.getEmailId(), user.getUserName(), user.getUserPswd());
            log.info("User saved successfully: {}", userRequest.getUserName());
            return true;

        } catch (Exception e) {
            log.error("Error occurred during saving & sending welcome message to user: {}. Error: {}", userRequest.getUserName(), e.getMessage());
            return false;
        }
    }

    // Delete User by Username (Delete Employee)
    @Override
    @Transactional
    public boolean deleteUserByUsername(String username) {
        try {
            boolean exists = userRepository.existsByUserName(username);
            if (!exists) {
                throw new RuntimeException("User not found!");
            }
            userRepository.deleteByUserName(username);
            return true;
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateUser(UserRequest userRequest) {
        String userName = userRequest.getUserName();

        if (userName == null || userName.isBlank()) {
            log.warn("Username is null or empty in update request");
            return false;
        }

        Optional<User> existingUser = userRepository.findByUserName(userName);

        if (existingUser.isEmpty()) {
            log.warn("User not found for username: {}", userName);
            return false;
        }

        try {
            User user = existingUser.get();

            // Update only non-null fields
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
            // Only update password if a new one is provided
            if (userRequest.getUserPassword() != null && !userRequest.getUserPassword().isBlank()) {
                user.setUserPswd(passwordEncoder.encode(userRequest.getUserPassword()));
            }
            user.setUpdateDate(OffsetDateTime.now());
            user.setUpdatedBy(userRequest.getCreatedBy()); // createdBy = who made the change

            userRepository.save(user);
            log.info("User updated successfully: {}", userName);
            return true;
        } catch (Exception e) {
            log.error("Error updating user: {}. Error: {}", userName, e.getMessage());
            return false;
        }
    }
}
