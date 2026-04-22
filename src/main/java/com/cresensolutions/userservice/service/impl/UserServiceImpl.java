package com.cresensolutions.userservice.service.impl;

import com.cresensolutions.userservice.dto.LoginRequest;
import com.cresensolutions.userservice.dto.LoginResponse;
import com.cresensolutions.userservice.dto.ManagerResponse;
import com.cresensolutions.userservice.entity.Role;
import com.cresensolutions.userservice.entity.User;
import com.cresensolutions.userservice.exception.CustomException;
import com.cresensolutions.userservice.repository.UserRepository;
import com.cresensolutions.userservice.service.UserService;
import com.cresensolutions.userservice.util.DateTimeUtil;
import com.cresensolutions.userservice.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    @Override
    public LoginResponse login(LoginRequest request) {

        String username = request.getUsername();
        String password = request.getPassword();

        Optional<User> dbUser = userRepository.findByUserName(username);

        if (dbUser.isEmpty()) {
            throw new CustomException("User does not exist", 404);
        }

        // If user founds with the proper credentials (username)
        User user = dbUser.get();
        if (!passwordEncoder.matches(password, user.getUserPswd())) {
            throw new CustomException("Invalid credentials", 401);
        }

        String correctUserName = user.getUserName();
        String correctEmailId = user.getEmailId();
        Long correctUserId = user.getId();
        Role mappedRole = user.getRole();  // mappedRole of Role schema
        log.info("mappedRole: {}", mappedRole);
        String correctUserRole = mappedRole != null ? mappedRole.getUniqueName() : null;  // get RoleName from Role schema
        log.info("Correct User Role after mapping: {}", correctUserRole);
        String correctUserFullName = user.getFullName();
        String correctUserEmail = user.getEmailId();

        String token = jwtUtil.generateToken(correctUserName, correctUserRole, correctUserId, correctUserEmail);

        user.setLastLogin(DateTimeUtil.nowInIst());
        userRepository.save(user);

        return new LoginResponse(
                token,
                correctUserRole,
                correctEmailId,
                correctUserFullName,
                correctUserId
        );
    }

    // Sending Managers to frontend to display Manager names in drop down of apply leave
    @Override
    public List<ManagerResponse> getManagers() {
        return userRepository.findByRoleId(2L)
                .stream()
                .map(user -> {
                    ManagerResponse response = new ManagerResponse();
                    response.setId(user.getId());
                    response.setFullName(user.getFullName());
                    response.setEmailId(user.getEmailId());
                    return response;
                }).toList();
    }
}
