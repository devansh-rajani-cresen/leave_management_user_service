package com.cresensolutions.userservice.dto;

import lombok.Data;

// We have to save all these values inside "user_profile" table
@Data
public class CreateUserRequest {
    private String companyId;
    private String userName;
    private String fullName;
    private String emailId;  // will be unique
    private String password;  // initially admin will set password
    private String role;  // Dropdown for ADMIN/MANAGER/EMPLOYEE
    private String gender;  // Dropdown for MEN/WOMEN/OTHERS
    private String createdBy;  // Dropdown for ADMIN/HR
}