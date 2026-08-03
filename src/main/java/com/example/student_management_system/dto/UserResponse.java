package com.example.student_management_system.dto;

import lombok.Data;

@Data
public class UserResponse {

    private Long id;

    private String username;

    private String email;

    private String role;

    private String studentCode;

//    private Boolean enabled;

}
