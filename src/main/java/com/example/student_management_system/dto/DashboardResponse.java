package com.example.student_management_system.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private Long totalStudents;

    private Long totalClassrooms;

    private Long totalSubjects;

    private List<ClassroomStudentCountResponse> studentsPerClassroom;

    private List<TopStudentResponse> topStudents;

}
