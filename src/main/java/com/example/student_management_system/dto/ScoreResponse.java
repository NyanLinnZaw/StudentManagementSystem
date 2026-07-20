package com.example.student_management_system.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoreResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Double assignmentScore;
    private Double midtermScore;
    private Double finalScore;
    private Double averageScore;
    private Double gpa;

}
