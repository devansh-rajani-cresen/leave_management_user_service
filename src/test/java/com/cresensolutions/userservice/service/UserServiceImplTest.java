package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.*;
import com.cresensolutions.userservice.entity.Role;
import com.cresensolutions.userservice.entity.User;
import com.cresensolutions.userservice.exception.CustomException;
import com.cresensolutions.userservice.repository.UserRepository;
import com.cresensolutions.userservice.service.impl.UserServiceImpl;
import com.cresensolutions.userservice.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static com.cresensolutions.userservice.common.UserConstants.MANAGER_ROLE_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    // Helper to encode password to Base64 as the service expects
    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    // LOGIN TESTS
    @Test
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword(encode("plainPassword"));

        Role role = new Role();
        role.setUniqueName("EMPLOYEE");

        User user = new User();
        user.setId(1L);
        user.setUserName("testuser");
        user.setUserPswd("encodedPassword");
        user.setRole(role);
        user.setFullName("Test User");
        user.setEmailId("test@mail.com");
        user.setActive(true);

        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString(), anyLong(), anyString(), anyString())).thenReturn("mockToken");

        LoginResponse res = userService.login(request);

        assertNotNull(res);
        assertEquals("mockToken", res.getToken());
        assertEquals("EMPLOYEE", res.getRole());
        verify(userRepository, times(1)).save(user); // Verifies lastLogin update
    }

    @Test
    void login_userNotFound_throws404() {
        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword(encode("pw"));

        when(userRepository.findByUserName("unknown")).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class, () -> userService.login(request));
        assertEquals(404, ex.getStatus());
        assertEquals("User does not exist!", ex.getMessage());
    }

    @Test
    void login_invalidPassword_throws401() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword(encode("wrong"));

        User user = new User();
        user.setUserPswd("correctEncoded");
        user.setActive(true);

        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "correctEncoded")).thenReturn(false);

        CustomException ex = assertThrows(CustomException.class, () -> userService.login(request));
        assertEquals(401, ex.getStatus());
        assertEquals("Invalid credentials!", ex.getMessage());
    }

    @Test
    void login_roleNull_successWithNullRole() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword(encode("pw"));

        User user = new User();
        user.setRole(null); // Testing the mappedRole != null ? ... : null logic
        user.setActive(true);

        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), any())).thenReturn(true);

        LoginResponse res = userService.login(request);
        assertNull(res.getRole());
    }

    // Inactive Role try to Log in
    @Test
    void login_inactiveUser_throws403(){
        LoginRequest request = new LoginRequest();
        request.setUsername("devansh");
        request.setPassword(encode("demo"));

        User user = new User();
        user.setUserName("devansh");
        user.setUserPswd("encodedPassword");
        user.setActive(false);

        when(userRepository.findByUserName("devansh")).thenReturn(Optional.of(user));

        CustomException exception = assertThrows(
                CustomException.class,
                () -> userService.login(request)
        );

        assertEquals(403, exception.getStatus());
        assertEquals(
                "Sorry, your account is not activated yet. Please contact HR!",
                exception.getMessage()
        );
    }

    @Test
    void login_activeUser_returnsLoginResponse() {

        LoginRequest request = new LoginRequest();
        request.setUsername("devansh");
        request.setPassword(encode("demo"));

        Role role = new Role();
        role.setUniqueName("EMPLOYEE");

        User user = new User();
        user.setId(1L);
        user.setUserName("testuser");
        user.setUserPswd("encodedPassword");
        user.setRole(role);
        user.setFullName("Test User");
        user.setEmailId("test@mail.com");
        user.setActive(true);

        when(userRepository.findByUserName("devansh"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("demo", "encodedPassword"))
                .thenReturn(true);

        when(jwtUtil.generateToken(
                anyString(),
                anyString(),
                anyLong(),
                anyString(),
                anyString()
        )).thenReturn("jwt-token");

        LoginResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());

        verify(userRepository).findByUserName("devansh");
    }

    // GET MANAGERS TESTS

    @Test
    void getManagers_success() {
        User m1 = new User();
        m1.setId(10L);
        m1.setFullName("Manager One");

        when(userRepository.findByRoleId(MANAGER_ROLE_ID)).thenReturn(List.of(m1));

        List<ManagerResponse> result = userService.getManagers();

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals("Manager One", result.get(0).getFullName());
    }

    // EMPLOYEE COUNT TESTS

    @Test
    void getEmployeeCount_success() {
        when(userRepository.count()).thenReturn(50L);

        long count = userService.getEmployeeCount();

        assertEquals(50L, count);
        verify(userRepository, times(1)).count();
    }

    // SEARCH USER TESTS

    @Test
    void searchUserByName_success() {
        User u1 = new User();
        u1.setId(1L);
        u1.setFullName("Devansh");
        u1.setRoleName("ADMIN");

        when(userRepository.findByFullNameContainingIgnoreCase("Devansh")).thenReturn(List.of(u1));

        List<BasicUserInfoForAI> result = userService.searchUserByName("Devansh");

        assertEquals(1, result.size());
        assertEquals("Devansh", result.get(0).getFullName());
        verify(userRepository).findByFullNameContainingIgnoreCase("Devansh");
    }

    // GET INFO BY ROLE TESTS

    @Test
    void getUserInfoByRole_success() {
        User u1 = new User();
        u1.setId(2L);
        u1.setFullName("Devansh");
        u1.setRoleName("ADMIN");

        when(userRepository.findByRoleNameIgnoreCase("ADMIN")).thenReturn(List.of(u1));

        List<BasicUserInfoForAI> result = userService.getUserInfoByRole("ADMIN");

        assertEquals(1, result.size());
        assertEquals("ADMIN", result.get(0).getRole());
        verify(userRepository).findByRoleNameIgnoreCase("ADMIN");
    }
}