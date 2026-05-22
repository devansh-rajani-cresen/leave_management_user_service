package com.cresensolutions.userservice.dto;

import lombok.Data;

@Data
public class MyProfile {
    private String companyId;
    private String userName;
    private String fullName;
    private String emailId;
    private String role;
    private String gender;
    private boolean active;
}
