package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.LoginRequest;
import com.cresensolutions.userservice.dto.LoginResponse;
import com.cresensolutions.userservice.entity.User;
import com.cresensolutions.userservice.repository.UserRepository;
import com.cresensolutions.userservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private LoginRequest loginRequest;
    private User mockUser;

    private final String email = "test@example.com";
    private final String rawPassword = "password123";
    private final String encodedPassword = "$2a$10$encodedpassword";
    private final String role = "EMPLOYEE";
    private final String token = "mock.jwt.token";

    @BeforeEach
    void setUp() {
        // Setup LoginRequest
        loginRequest = new LoginRequest();
        loginRequest.setUsername(email);
        loginRequest.setPassword(rawPassword);

        // Setup mock User from DB
        mockUser = new User();
        mockUser.setUserName(email);
        mockUser.setUserPswd(encodedPassword);
        mockUser.setRole(role);
    }

    // Valid credentials - should return LoginResponse with token
    @Test
    void login_ValidCredentials_ShouldReturnLoginResponse() {
        when(userRepository.findByUserName(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(email, role)).thenReturn(token);

        LoginResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals(role, response.getRole());
        assertEquals(email, response.getEmail());

        verify(jwtUtil, times(1)).generateToken(email, role);
    }

    // User not found - should throw RuntimeException
    @Test
    void login_UserNotFound_ShouldThrowException() {
        when(userRepository.findByUserName(email)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("User not exists in DB!", exception.getMessage());
        verify(passwordEncoder, never()).matches(any(), any()); // should not check password
        verify(jwtUtil, never()).generateToken(any(), any());   // should not generate token
    }

    // Wrong password - should throw RuntimeException
    @Test
    void login_WrongPassword_ShouldThrowException() {
        when(userRepository.findByUserName(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false); // wrong password

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("Invalid Credentials!", exception.getMessage());
        verify(jwtUtil, never()).generateToken(any(), any()); // should not generate token
    }

    // Valid ADMIN role - should return correct role in response
    @Test
    void login_AdminUser_ShouldReturnAdminRole() {
        mockUser.setRole("ADMIN");

        when(userRepository.findByUserName(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(email, "ADMIN")).thenReturn(token);

        LoginResponse response = userService.login(loginRequest);

        assertEquals("ADMIN", response.getRole());
        verify(jwtUtil, times(1)).generateToken(email, "ADMIN");
    }

    // Valid MANAGER role - should return correct role in response
    @Test
    void login_ManagerUser_ShouldReturnManagerRole() {
        mockUser.setRole("MANAGER");

        when(userRepository.findByUserName(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(email, "MANAGER")).thenReturn(token);

        LoginResponse response = userService.login(loginRequest);

        assertEquals("MANAGER", response.getRole());
        verify(jwtUtil, times(1)).generateToken(email, "MANAGER");
    }

    // Token should not be null or empty on success
    @Test
    void login_ValidCredentials_TokenShouldNotBeNullOrEmpty() {
        when(userRepository.findByUserName(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(email, role)).thenReturn(token);

        LoginResponse response = userService.login(loginRequest);

        assertNotNull(response.getToken());
        assertFalse(response.getToken().isEmpty());
    }
}