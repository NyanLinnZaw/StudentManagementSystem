package com.example.student_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    private Long id;
    private String studentId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String email;
    private String address;
    private String parentPhone;
    private LocalDate enrollmentDate;
    private String status;
    //private String avatar;
    private Long classroomId;
    private String classroomCode;
    private String classroomName;
}
