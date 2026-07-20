package com.example.student_management_system.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScoreRequest {

    @NotNull
    private Long studentId;

    @NotNull
    private Long subjectId;

    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private Double assignmentScore;

    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private Double midtermScore;

    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private Double finalScore;

}
