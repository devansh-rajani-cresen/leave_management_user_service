package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.LoginRequest;
import com.cresensolutions.userservice.dto.LoginResponse;
import com.cresensolutions.userservice.entity.User;
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

        log.info("Request : {}", request);

        String username = request.getUsername();
        String password = request.getPassword();
        log.info("Username: {}", username);

        Optional<User> dbUser = userRepository.findByUserName(username);
        log.info("Fetched user from DB: {}", dbUser);

        if (dbUser.isEmpty()) {
            throw new RuntimeException("User not exists in DB!");
        }

        // If user founds with the proper credentials (username)
        User user = dbUser.get();
        if (!passwordEncoder.matches(password, user.getUserPswd())) {
            throw new RuntimeException("Invalid Credentials!");
        }

        String correct_user_name = user.getUserName();
        String correct_user_role = user.getRole();

        String token = jwtUtil.generateToken(correct_user_name, correct_user_role);

        return new LoginResponse(
                token,
                correct_user_role,
                correct_user_name
        );
    }
}
