package com.example.student_management_system.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopStudentResponse {

    private Long studentId;

    private String studentCode;

    private String studentName;

    private Double gpa;

}
