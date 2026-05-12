package com.cresensolutions.userservice.common;

public class UserConstants {
    private UserConstants() {}

    // Basic fields
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String FULL_NAME = "fullName";
    public static final Long MANAGER_ROLE_ID = 2L;
    public static final String AUTH_HEADER = "Authorization";
    public static final String HEADER_STARTING = "Bearer ";
    public static final Integer TOKEN_STARTING_INDEX = 7;

    // for Email Template
    public static final String LOGIN_URL = "loginUrl";
    public static final String LOGIN_URL_VALUE = "http://localhost:4200/auth/login";
    public static final String FROM_COMPANY_NAME = "Cresen Solutions";

    // OTP for reset password
    public static final String RESET_PASSWORD_OTP_SUBJECT = "Password Reset OTP for Cresen Solutions";
    public static final String OTP = "otp";
    public static final String SEND_OTP_ERROR = "Unable to Send OTP. Please try again later.";

    // Welcome mail
    public static final String WELCOME_MAIL_SUBJECT = "Welcome to Cresen Solutions!";
    public static final String SEND_WELCOME_MAIL_ERROR = "Unable to send welcome mail. Please contact support.";

    // Exit mail
    public static final String EXIT_MAIL_SUBJECT = "Exit Process initiated at Cresen Solutions";
    public static final String EXIT_MAIL_SEND_ERROR = "Unable to send account deactivation email. Please contact support.";
}
