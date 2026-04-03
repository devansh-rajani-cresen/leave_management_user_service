package com.cresensolutions.userservice.dto;

import lombok.Data;

@Data
public class UserRequest {
    private String companyId;
    private String fullName;
    private String userName;
    private String userPassword;
    private String emailId;
    private String role;
    private String gender;
    private Boolean active;
    private String createdBy;
}
