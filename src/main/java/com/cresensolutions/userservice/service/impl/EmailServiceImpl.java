package com.cresensolutions.userservice.service.impl;

import com.cresensolutions.userservice.exception.CustomException;
import com.cresensolutions.userservice.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.Map;
import static com.cresensolutions.userservice.common.UserConstants.*;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    // will execute this method to send OTP message during Forgot Password
    @Override
    public void sendOtp(String toEmail, String otp) {
        log.info("Sending OTP to Email: {}", toEmail);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, FROM_COMPANY_NAME);
            helper.setTo(toEmail);
            helper.setSubject(RESET_PASSWORD_OTP_SUBJECT);

            // Thymeleaf context
            Context context = new Context();
            context.setVariable(OTP, otp);

            String htmlContent = templateEngine.process("otp-email", context);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            log.error("Error while sending OTP email", e);
            throw new CustomException(
                    SEND_OTP_ERROR,
                    500
            );
        }
    }

    // have to send Welcome Email on newly created Employee's Email with Login button
    @Override
    public void sendWelcomeMessage(String toEmail, String username, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, FROM_COMPANY_NAME);
            helper.setTo(toEmail);
            helper.setSubject(WELCOME_MAIL_SUBJECT);

            Context context = new Context();
            context.setVariable(USERNAME, username);
            context.setVariable(PASSWORD, password);
            context.setVariable(LOGIN_URL, LOGIN_URL_VALUE);

            String htmlContent = templateEngine.process("welcome-mail", context);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            log.error("Error while sending Welcome email", e);
            throw new CustomException(
                    SEND_WELCOME_MAIL_ERROR,
                    500
            );
        }
    }

    @Override
    public void sendUpdateMessage(String toEmail, String username, Map<String, String> updatedFields) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, FROM_COMPANY_NAME);
            helper.setTo(toEmail);
            helper.setSubject(UPDATE_MAIL_SUBJECT);

            Context context = new Context();
            context.setVariable(USERNAME, username);
            context.setVariable("updatedFields", updatedFields);
            context.setVariable(LOGIN_URL, LOGIN_URL_VALUE);

            String htmlContent = templateEngine.process("update-user-mail", context);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            log.error("Error while sending Update email", e);
            throw new CustomException(
                    SEND_UPDATE_MAIL_ERROR,
                    500
            );
        }
    }

    @Override
    public void sendDeleteMessage(String fullName, String toEmail) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, FROM_COMPANY_NAME);
            helper.setTo(toEmail);
            helper.setSubject(EXIT_MAIL_SUBJECT);

            Context context = new Context();
            context.setVariable(FULL_NAME, fullName);

            String htmlContent = templateEngine.process("delete-mail", context);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            log.error("Error while sending Delete email", e);
            throw new CustomException(
                    EXIT_MAIL_SEND_ERROR,
                    500
            );
        }
    }
}
