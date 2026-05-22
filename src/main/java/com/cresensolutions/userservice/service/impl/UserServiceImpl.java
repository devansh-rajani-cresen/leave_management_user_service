package com.cresensolutions.userservice.service.impl;

import com.cresensolutions.userservice.dto.BasicUserInfoForAI;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import static com.cresensolutions.userservice.common.UserConstants.MANAGER_ROLE_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {

        String username = request.getUsername();
        String password = request.getPassword();
        String decodedPassword = new String(Base64.getDecoder().decode(password));

        Optional<User> dbUser = userRepository.findByUserName(username);

        if (dbUser.isEmpty()) {
            throw new CustomException("User does not exist!", 404);
        }

        // If user founds with the proper credentials (username)
        User user = dbUser.get();

        boolean isActive = user.getActive();
        if (!isActive){
            throw new CustomException("Sorry, your account is not activated yet. Please contact HR!", 403);
        }

        String correctUserPswd = user.getUserPswd();
        if (!passwordEncoder.matches(decodedPassword, correctUserPswd)) {
            throw new CustomException("Invalid credentials!", 401);
        }

        String correctUserName = user.getUserName();
        Long correctUserId = user.getId();
        Role mappedRole = user.getRole();  // mappedRole of Role schema
        String correctUserRole = mappedRole != null ? mappedRole.getUniqueName() : null;  // get RoleName from Role schema
        String correctUserFullName = user.getFullName();
        String correctUserEmail = user.getEmailId();

        String token = jwtUtil.generateToken(correctUserName, correctUserRole, correctUserId, correctUserEmail, correctUserFullName);

        user.setLastLogin(DateTimeUtil.nowInIst());
        userRepository.save(user);

        return new LoginResponse(
                token,
                correctUserName,
                correctUserRole,
                correctUserEmail,
                correctUserFullName,
                correctUserId
        );
    }

    // Sending Managers to frontend to display Manager names in drop down of apply leave
    @Override
    public List<ManagerResponse> getManagers() {
        return userRepository.findByRoleId(MANAGER_ROLE_ID)
                .stream()
                .map(user -> {
                    ManagerResponse response = new ManagerResponse();
                    response.setId(user.getId());
                    response.setFullName(user.getFullName());
                    response.setEmailId(user.getEmailId());
                    return response;
                }).toList();
    }

    // For HR Questions in Chatbot

    // Get Total Employee count
    @Override
    public long getEmployeeCount() {
        long empCount = userRepository.count();
        log.info("Employee count: {}", empCount);
        return empCount;
    }

    // Search user by name
    @Override
    public List<BasicUserInfoForAI> searchUserByName(String name) {
        List<User> users = userRepository.findByFullNameContainingIgnoreCase(name);
        log.info("Users searched by name: {}", users);
        return users.stream()
                .map(user -> new BasicUserInfoForAI(
                        user.getId(),
                        user.getFullName(),
                        user.getRoleName(),
                        user.getEmailId()
                )).toList();
    }

    // Get details about particular Employee
    @Override
    public List<BasicUserInfoForAI> getUserInfoByRole(String role) {
        List<User> users = userRepository.findByRoleNameIgnoreCase(role);
        log.info("Getting user info for Role: {}", role);
        return users.stream()
                .map(user -> new BasicUserInfoForAI(
                        user.getId(),
                        user.getFullName(),
                        user.getRoleName(),
                        user.getEmailId()
                )).toList();
    }

}
