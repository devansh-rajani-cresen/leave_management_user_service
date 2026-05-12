package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.exception.CustomException;
import com.cresensolutions.userservice.service.impl.EmailServiceImpl;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    // SEND OTP

    @Test
    void sendOtp_success() throws Exception {
        setFromEmail();

        MimeMessage message = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(message);
        when(templateEngine.process(eq("otp-email"), any())).thenReturn("<html>otp</html>");

        emailService.sendOtp("test@mail.com", "123456");

        verify(mailSender).send(message);
    }

    @Test
    void sendOtp_exception() throws Exception {
        setFromEmail();

        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException());

        CustomException ex = assertThrows(CustomException.class,
                () -> emailService.sendOtp("test@mail.com", "123456"));

        assertEquals("Unable to Send OTP. Please try again later.", ex.getMessage());
        assertEquals(500, ex.getStatus());
    }

    // SEND WELCOME

    @Test
    void sendWelcomeMessage_success() throws Exception {
        setFromEmail();

        MimeMessage message = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(message);
        when(templateEngine.process(eq("welcome-mail"), any())).thenReturn("<html>welcome</html>");

        emailService.sendWelcomeMessage("test@mail.com", "dev", "123");

        verify(mailSender).send(message);
    }

    @Test
    void sendWelcomeMessage_exception() throws Exception {
        setFromEmail();

        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException());

        CustomException ex = assertThrows(CustomException.class,
                () -> emailService.sendWelcomeMessage("test@mail.com", "dev", "123"));

        assertEquals("Unable to send welcome mail. Please contact support.", ex.getMessage());
        assertEquals(500, ex.getStatus());
    }

    // SEND DELETE

    @Test
    void sendDeleteMessage_success() throws Exception {
        setFromEmail();

        MimeMessage message = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(message);
        when(templateEngine.process(eq("delete-mail"), any())).thenReturn("<html>delete</html>");

        emailService.sendDeleteMessage("Dev User", "test@mail.com");

        verify(mailSender).send(message);
    }

    @Test
    void sendDeleteMessage_exception() throws Exception {
        setFromEmail();

        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException());

        CustomException ex = assertThrows(CustomException.class,
                () -> emailService.sendDeleteMessage("Dev User", "test@mail.com"));

        assertEquals("Unable to send account deactivation email. Please contact support.", ex.getMessage());
        assertEquals(500, ex.getStatus());
    }

    // HELPER

    private void setFromEmail() throws Exception {
        Field field = EmailServiceImpl.class.getDeclaredField("fromEmail");
        field.setAccessible(true);
        field.set(emailService, "test@cresen.com");
    }
}
