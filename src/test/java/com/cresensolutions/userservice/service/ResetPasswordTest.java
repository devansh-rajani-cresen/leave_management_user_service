package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.ResetPasswordRequest;
import com.cresensolutions.userservice.entity.User;
import com.cresensolutions.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResetPasswordTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ResetPassword resetPassword;

    private ResetPasswordRequest request;
    private User mockUser;

    private final String email = "test@gmail.com";
    private final String newPassword = "newPassword123";
    private final String encodedPassword = "$2a$10$encodedpassword";

    @BeforeEach
    void setUp() {
        request = new ResetPasswordRequest();
        request.setEmail(email);
        request.setNewPassword(newPassword);

        mockUser = new User();
        mockUser.setUserName(email);
        mockUser.setUserPswd("oldEncodedPassword");
    }

    // Valid user - password should be encoded and saved
    @Test
    void resetPassword_ValidUser_ShouldEncodeAndSavePassword() {
        when(userRepository.findByUserName(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        boolean result = resetPassword.resetPassword(request);

        assertTrue(result);
        assertEquals(encodedPassword, mockUser.getUserPswd()); // password must be encoded
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(mockUser);
    }

    // User not found - should return false
    @Test
    void resetPassword_UserNotFound_ShouldReturnFalse() {
        when(userRepository.findByUserName(email)).thenReturn(Optional.empty());

        boolean result = resetPassword.resetPassword(request);

        assertFalse(result);
        verify(passwordEncoder, never()).encode(any()); // should not encode
        verify(userRepository, never()).save(any());    // should not save
    }

    // Null password - should return false without hitting DB
    @Test
    void resetPassword_NullPassword_ShouldReturnFalse() {
        request.setNewPassword(null);

        boolean result = resetPassword.resetPassword(request);

        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    // Blank password - should return false
    @Test
    void resetPassword_BlankPassword_ShouldReturnFalse() {
        request.setNewPassword("   ");

        boolean result = resetPassword.resetPassword(request);

        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    // DB failure while saving - should return false
    @Test
    void resetPassword_DbFailure_ShouldReturnFalse() {
        when(userRepository.findByUserName(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        doThrow(new RuntimeException("DB error")).when(userRepository).save(any());

        boolean result = resetPassword.resetPassword(request);

        assertFalse(result);
    }

    // New password should be different from old password after reset
    @Test
    void resetPassword_ShouldReplaceOldPassword() {
        String oldPassword = mockUser.getUserPswd();

        when(userRepository.findByUserName(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        resetPassword.resetPassword(request);

        assertNotEquals(oldPassword, mockUser.getUserPswd()); // password must change
    }
}