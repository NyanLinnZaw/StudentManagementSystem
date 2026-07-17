package com.example.student_management_system.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubjectResponse {

    private Long id;

    private String subjectCode;

    private String subjectName;

    private String description;

}
