package com.cresensolutions.userservice.dto;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String userName;
    private String fullName;
    private String emailId;
    private Long roleId;
    private String role;
    private String gender;
    private Boolean active;
    private String companyId;
}
