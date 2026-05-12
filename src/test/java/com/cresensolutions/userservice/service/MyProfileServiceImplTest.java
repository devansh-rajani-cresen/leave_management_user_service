package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.MyProfile;
import com.cresensolutions.userservice.entity.User;
import com.cresensolutions.userservice.exception.CustomException;
import com.cresensolutions.userservice.repository.UserRepository;
import com.cresensolutions.userservice.service.impl.MyProfileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MyProfileServiceImpl service;

    @Test
    void getMyProfileData_success() {

        User user = new User();

        user.setCompanyId("CS001");
        user.setUserName("dev123");
        user.setFullName("Devansh Rajani");
        user.setEmailId("dev@mail.com");
        user.setRoleName("EMPLOYEE");
        user.setGender("Male");
        user.setActive(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        MyProfile response =
                service.getMyProfileData(1L);

        assertNotNull(response);

        assertEquals("CS001", response.getCompanyId());
        assertEquals("dev123", response.getUserName());
        assertEquals("Devansh Rajani", response.getFullName());
        assertEquals("dev@mail.com", response.getEmailId());
        assertEquals("EMPLOYEE", response.getRole());
        assertEquals("Male", response.getGender());

        verify(userRepository).findById(1L);
    }

    @Test
    void getMyProfileData_userNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        CustomException exception =
                assertThrows(
                        CustomException.class,
                        () -> service.getMyProfileData(1L)
                );

        assertEquals("User not found!", exception.getMessage());

        verify(userRepository).findById(1L);
    }
}
