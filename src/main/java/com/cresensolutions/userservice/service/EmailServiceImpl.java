package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.exception.CustomException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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

            helper.setFrom(fromEmail, "Cresen Solutions");
            helper.setTo(toEmail);
            helper.setSubject("Password Reset OTP for Cresen Solutions");

            // Thymeleaf context
            Context context = new Context();
            context.setVariable("otp", otp);

            String htmlContent = templateEngine.process("otp-email", context);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            log.error("Error while sending OTP email", e);
            throw new CustomException(
                    "Unable to Send OTP. Please try again later.",
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

            helper.setFrom(fromEmail, "Cresen Solutions");
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Cresen Solutions!");

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("password", password);
            context.setVariable("loginUrl", "http://localhost:4200/auth/login");

            String htmlContent = templateEngine.process("welcome-mail", context);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            log.error("Error while sending Welcome email", e);
            throw new CustomException(
                    "Unable to send welcome mail. Please contact support.",
                    500
            );
        }
    }

    @Override
    public void sendDeleteMessage(String fullName, String toEmail) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Cresen Solutions");
            helper.setTo(toEmail);
            helper.setSubject("Exit Process initiated at Cresen Solutions");

            Context context = new Context();
            context.setVariable("fullName", fullName);

            String htmlContent = templateEngine.process("delete-mail", context);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            log.error("Error while sending Delete email", e);
            throw new CustomException(
                    "Unable to send account deactivation email. Please contact support.",
                    500
            );
        }
    }
}