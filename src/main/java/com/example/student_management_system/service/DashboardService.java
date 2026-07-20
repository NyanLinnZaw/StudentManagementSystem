package com.example.student_management_system.service;

import com.example.student_management_system.dto.*;
import com.example.student_management_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository;
    private final SubjectRepository subjectRepository;
    private final ScoreRepository scoreRepository;

    public DashboardResponse getDashboard(){

        // Total students
        Long totalStudents = studentRepository.countByDeletedFalse();
        // Total classrooms
        Long totalClassrooms = classroomRepository.countByDeletedFalse();
        // Total subjects
        Long totalSubjects = subjectRepository.countByDeletedFalse();
        // Students per classroom
        List<ClassroomStudentCountResponse> studentsPerClassroom = getStudentsPerClassroom();
        // Top students
        List<TopStudentResponse> topStudents = getTopStudents();

        return DashboardResponse.builder()
                .totalStudents(totalStudents)
                .totalClassrooms(totalClassrooms)
                .totalSubjects(totalSubjects)
                .studentsPerClassroom(studentsPerClassroom)
                .topStudents(topStudents)
                .build();
    }

    private List<ClassroomStudentCountResponse>
    getStudentsPerClassroom(){
        return classroomRepository
                .countStudentsByClassroom()
                .stream()
                .map(row ->
                        ClassroomStudentCountResponse.builder()
                                .classroomId(((Number) row[0]).longValue())
                                .classroomName((String) row[1])
                                .studentCount(((Number) row[2]).longValue())
                                .build()).toList();
    }

    private List<TopStudentResponse>
    getTopStudents(){
        return scoreRepository
                .findTopStudents()
                .stream()
                .limit(5)
                .map(row ->
                        TopStudentResponse.builder()
                                .studentId(((Number) row[0]).longValue())
                                .studentCode((String) row[1])
                                .studentName((String) row[2])
                                .gpa(((Number) row[3]).doubleValue())
                                .build()).toList();
    }
}
