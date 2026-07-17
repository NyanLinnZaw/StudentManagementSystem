package com.example.student_management_system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClassroomRequest {

    @NotBlank(message = "Classroom code is required")
    private String classroomCode;

    @NotBlank(message = "Classroom name is required")
    private String classroomName;

    @Min(value = 1)
    private Integer capacity;

}
