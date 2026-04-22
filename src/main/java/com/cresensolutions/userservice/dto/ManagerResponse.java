package com.cresensolutions.userservice.dto;

import lombok.Data;

@Data
public class ManagerResponse {
    private Long id;  // storing selected manager's id in manager_id column (in leaves.leave table)
    private String fullName;  // for displaying name in dropdown
    private String emailId;  // for sending mail to selected manager
}
