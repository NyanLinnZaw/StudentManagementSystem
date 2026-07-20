package com.example.student_management_system.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudentScoreReportResponse {

    private Long studentId;

    private String studentCode;

    private String studentName;

    private String classroomName;

    // List of all subject scores
    private List<ScoreResponse> scores;

    // Overall average score of all subjects
    private Double overallAverage;

    // Overall GPA
    private Double overallGpa;
}
