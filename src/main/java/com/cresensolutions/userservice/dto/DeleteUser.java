package com.cresensolutions.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeleteUser {
    private String fullName;
    private String userName;
    private String emailId;
}
