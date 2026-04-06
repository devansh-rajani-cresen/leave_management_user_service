package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.LoginRequest;
import com.cresensolutions.userservice.dto.LoginResponse;
import com.cresensolutions.userservice.entity.User;
import com.cresensolutions.userservice.exception.CustomException;
import com.cresensolutions.userservice.repository.UserRepository;
import com.cresensolutions.userservice.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
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
        String correctUserRole = user.getRole();
        String correctUserFullName = user.getFullName();

        String token = jwtUtil.generateToken(correctUserName, correctUserRole);

        return new LoginResponse(
                token,
                correctUserRole,
                correctUserName,
                correctUserFullName
        );
    }
}
