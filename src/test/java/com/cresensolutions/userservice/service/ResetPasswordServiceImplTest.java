package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.ResetPasswordRequest;
import com.cresensolutions.userservice.entity.User;
import com.cresensolutions.userservice.exception.CustomException;
import com.cresensolutions.userservice.repository.UserRepository;
import com.cresensolutions.userservice.service.impl.ResetPasswordServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ResetPasswordServiceImpl service;

    // SUCCESS

    @Test
    void resetPassword_success() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setEmail("test@mail.com");
        req.setNewPassword("123");

        User user = new User();
        user.setEmailId("test@mail.com");

        when(userRepository.findByEmailId("test@mail.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode("123"))
                .thenReturn("encoded");

        service.resetPassword(req);

        verify(userRepository).save(user);
        assertEquals("encoded", user.getUserPswd());
    }

    // USER NOT FOUND

    @Test
    void resetPassword_userNotFound() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setEmail("test@mail.com");

        when(userRepository.findByEmailId("test@mail.com"))
                .thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> service.resetPassword(req));

        assertEquals("User not found with email!", ex.getMessage());
    }

    // EXCEPTION

    @Test
    void resetPassword_exception() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setEmail("test@mail.com");
        req.setNewPassword("123");

        User user = new User();

        when(userRepository.findByEmailId("test@mail.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode("123"))
                .thenReturn("encoded");

        doThrow(new RuntimeException())
                .when(userRepository).save(user);

        CustomException ex = assertThrows(CustomException.class,
                () -> service.resetPassword(req));

        assertEquals("Failed to reset password!", ex.getMessage());
    }
}
