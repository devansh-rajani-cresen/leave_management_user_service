package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.UserRequest;
import com.cresensolutions.userservice.entity.Role;
import com.cresensolutions.userservice.entity.User;
import com.cresensolutions.userservice.repository.RoleRepository;
import com.cresensolutions.userservice.repository.UserRepository;
import com.cresensolutions.userservice.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.cresensolutions.userservice.dto.DeleteUser;
import com.cresensolutions.userservice.exception.CustomException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AdminServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "createLeaveUrl", "http://localhost/leaves");
    }

    // GET USERS

    @Test
    void getUsers_success() {
        Role role = new Role();
        role.setId(1L);
        role.setRoleName("ADMIN");

        User user = new User();
        user.setId(1L);
        user.setUserName("dev");
        user.setFullName("Dev");
        user.setEmailId("dev@test.com");
        user.setRole(role);

        when(userRepository.findAll()).thenReturn(List.of(user));

        assertEquals(1, service.getUsers().size());
    }

    @Test
    void getUsers_roleNull() {
        User user = new User();
        user.setRole(null);
        user.setRoleName("EMP");

        when(userRepository.findAll()).thenReturn(List.of(user));

        assertEquals("EMP", service.getUsers().get(0).getRole());
    }

    // SAVE USER

    @Test
    void saveUser_success() {
        UserRequest req = new UserRequest();
        req.setCompanyId("CS001");
        req.setUserName("dev");
        req.setUserPassword("123");
        req.setEmailId("test@mail.com");
        req.setFullName("Dev");
        req.setGender("Male");
        req.setActive(true);
        req.setRoleId(1L);

        Role role = new Role();
        role.setId(1L);
        role.setRoleName("ADMIN");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("123")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("created"));

        service.saveUser(req);

        verify(userRepository).save(any());
        verify(restTemplate).postForEntity(anyString(), any(), eq(String.class));
        verify(emailService).sendWelcomeMessage(any(), any(), any());
    }

    @Test
    void saveUser_roleNotFound() {
        UserRequest req = new UserRequest();
        req.setRoleId(1L);

        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> service.saveUser(req));
    }

    @Test
    void saveUser_internalException() {
        UserRequest req = new UserRequest();
        req.setUserName("dev");
        req.setUserPassword("123");
        req.setRoleId(1L);

        Role role = new Role();
        role.setId(1L);
        role.setRoleName("ADMIN");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("123")).thenReturn("encoded");
        when(userRepository.save(any())).thenThrow(new RuntimeException());

        assertThrows(CustomException.class, () -> service.saveUser(req));
    }

    // DELETE USER

    @Test
    void deleteUser_success() {
        DeleteUser req = new DeleteUser("Dev", "dev", "mail@test.com");

        when(userRepository.existsByUserName("dev")).thenReturn(true);

        service.deleteUser(req);

        verify(userRepository).deleteByUserName("dev");
        verify(emailService).sendDeleteMessage("Dev", "mail@test.com");
    }

    @Test
    void deleteUser_notFound() {
        DeleteUser req = new DeleteUser("Dev", "dev", "mail@test.com");

        when(userRepository.existsByUserName("dev")).thenReturn(false);

        assertThrows(CustomException.class, () -> service.deleteUser(req));
    }

    @Test
    void deleteUser_exception() {
        DeleteUser req = new DeleteUser("Dev", "dev", "mail@test.com");

        when(userRepository.existsByUserName("dev")).thenReturn(true);
        doThrow(new RuntimeException()).when(userRepository).deleteByUserName("dev");

        assertThrows(CustomException.class, () -> service.deleteUser(req));
    }

    // UPDATE USER

    @Test
    void updateUser_success() {
        UserRequest req = new UserRequest();
        req.setUserName("dev");
        req.setFullName("New Name");
        req.setRoleId(1L);
        req.setUserPassword("123");

        Role role = new Role();
        role.setRoleName("ADMIN");

        User user = new User();
        user.setUserName("dev");

        when(userRepository.findByUserName("dev")).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("123")).thenReturn("encoded");

        service.updateUser(req);

        verify(userRepository).save(user);
    }

    @Test
    void updateUser_usernameMissing() {
        UserRequest req = new UserRequest();

        assertThrows(CustomException.class, () -> service.updateUser(req));
    }

    @Test
    void updateUser_userNotFound() {
        UserRequest req = new UserRequest();
        req.setUserName("dev");

        when(userRepository.findByUserName("dev")).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> service.updateUser(req));
    }

    @Test
    void updateUser_exception() {
        UserRequest req = new UserRequest();
        req.setUserName("dev");

        User user = new User();
        user.setUserName("dev");

        when(userRepository.findByUserName("dev")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenThrow(new RuntimeException());

        assertThrows(CustomException.class, () -> service.updateUser(req));
    }
}
