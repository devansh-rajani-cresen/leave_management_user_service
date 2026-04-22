package com.cresensolutions.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String role;
    private String email;
    private String fullName;
    private Long userId;
}
