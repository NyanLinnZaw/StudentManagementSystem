package com.example.student_management_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentRequest {

    private String studentId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private LocalDate dateOfBirth;
    private String gender;

    @Email(message = "Email must be valid")
    private String email;

    private String address;
    private String parentPhone;
    private Long classroomId;
    private LocalDate enrollmentDate;
    private String status;
    //private String avatar;
}
