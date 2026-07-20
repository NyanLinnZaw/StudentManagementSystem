package com.example.student_management_system.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassroomStudentCountResponse {

    private Long classroomId;

    private String classroomName;

    private Long studentCount;

}
