package com.example.student_management_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @Email
    private String email;

    @NotBlank
    private String role;

    /** Required when role is STUDENT — links account to existing student by student code */
    private String studentCode;
}
