package com.example.student_management_system.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassroomResponse {

    private Long id;

    private String classroomCode;

    private String classroomName;

    private Integer capacity;

    private Integer totalStudents;

}
