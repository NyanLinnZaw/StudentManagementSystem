package com.example.student_management_system.service;

import com.example.student_management_system.dto.AssignStudentsRequest;
import com.example.student_management_system.dto.ClassroomRequest;
import com.example.student_management_system.dto.ClassroomResponse;
import com.example.student_management_system.dto.StudentResponse;
import com.example.student_management_system.entity.Classroom;
import com.example.student_management_system.entity.Student;
import com.example.student_management_system.exception.ResourceNotFoundException;
import com.example.student_management_system.repository.ClassroomRepository;
import com.example.student_management_system.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;

    public ClassroomResponse createClassroom(ClassroomRequest request) {

        if (classroomRepository.existsByClassroomCode(request.getClassroomCode())) {
            throw new IllegalArgumentException("Classroom code already exists");
        }

        Classroom classroom = new Classroom();
        classroom.setClassroomCode(request.getClassroomCode());
        classroom.setClassroomName(request.getClassroomName());
        classroom.setCapacity(request.getCapacity());
        classroom.setDeleted(false);

        Classroom saved = classroomRepository.save(classroom);

        return mapToResponse(saved);
    }

    public ClassroomResponse updateClassroom(Long id, ClassroomRequest request) {

        Classroom classroom = classroomRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Classroom not found"));

        classroom.setClassroomCode(request.getClassroomCode());
        classroom.setClassroomName(request.getClassroomName());
        classroom.setCapacity(request.getCapacity());

        Classroom updated = classroomRepository.save(classroom);

        return mapToResponse(updated);
    }

    public void deleteClassroom(Long id) {

        Classroom classroom = classroomRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Classroom not found"));

        if (classroom.getStudents() != null &&
                !classroom.getStudents().isEmpty()) {

            throw new IllegalArgumentException(
                    "Cannot delete classroom because students are assigned."
            );
        }

        classroom.setDeleted(true);

        classroomRepository.save(classroom);
    }

    public ClassroomResponse getClassroomById(Long id) {

        Classroom classroom = classroomRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Classroom not found"));

        return mapToResponse(classroom);
    }

    public Page<ClassroomResponse> getAllClassrooms(String keyword, int page, int size,
                                                    String sortBy, String sortDir){

        Sort sort = Sort.by("id").ascending();
        if(sortBy != null && !sortBy.isBlank()){
            if("desc".equalsIgnoreCase(sortDir)){
                sort = Sort.by(sortBy).descending();
            }else{
                sort = Sort.by(sortBy).ascending();
            }
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Classroom> classrooms = classroomRepository.search(keyword, pageable);
        return classrooms.map(this::mapToResponse);
    }

    private ClassroomResponse mapToResponse(Classroom classroom) {
        int totalStudents = 0;
        if (classroom.getStudents() != null) {
            totalStudents = classroom.getStudents().size();
        }
        return ClassroomResponse.builder()
                .id(classroom.getId())
                .classroomCode(classroom.getClassroomCode())
                .classroomName(classroom.getClassroomName())
                .capacity(classroom.getCapacity())
                .totalStudents(totalStudents)
                .build();
    }

    public Page<StudentResponse> getStudentsByClassroom(Long classroomId, int page, int size, String sortBy, String sortDir
    ) {
        Classroom classroom = classroomRepository
                .findByIdAndDeletedFalse(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));
        Sort sort = Sort.by("id").ascending();

        if (sortBy != null && !sortBy.isBlank()) {
            if ("desc".equalsIgnoreCase(sortDir)) {
                sort = Sort.by(sortBy).descending();
            } else {
                sort = Sort.by(sortBy).ascending();
            }
        }
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Student> students =
                studentRepository.findByClassroomIdAndDeletedFalse(
                        classroom.getId(),
                        pageable
                );

        return students.map(this::mapStudentResponse);
    }

    private StudentResponse mapStudentResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .studentId(student.getStudentId())
                .fullName(student.getFullName())
                .dateOfBirth(student.getDateOfBirth())
                .gender(student.getGender())
                .email(student.getEmail())
                .address(student.getAddress())
                .parentPhone(student.getParentPhone())
                //.avatar(student.getAvatar())
                .enrollmentDate(student.getEnrollmentDate())
                .status(student.getStatus())
                .classroomId(student.getClassroom() != null ? student.getClassroom().getId() : null)
                .classroomName(student.getClassroom() != null ? student.getClassroom().getClassroomName() : null)
                .build();

    }

    @Transactional
    public void assignStudents(Long classroomId, AssignStudentsRequest request) {
        // 1. Find classroom
        Classroom classroom = classroomRepository.findByIdAndDeletedFalse(classroomId)
                        .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));

        // 2. Find students
        List<Student> students = studentRepository.findAllByIdIn(request.getStudentIds());

        if(students.size() != request.getStudentIds().size()){
            throw new ResourceNotFoundException("Some students not found");
        }

        // 3. Check classroom capacity
        long currentStudentCount = studentRepository.countByClassroomIdAndDeletedFalse(classroomId);
        long totalAfterAssign = currentStudentCount + students.size();
        if(totalAfterAssign > classroom.getCapacity()){
            throw new IllegalArgumentException("Classroom capacity exceeded. Maximum capacity is " + classroom.getCapacity());
        }

        // 4. Assign students
        for(Student student : students){
            if(student.getClassroom() != null){
                throw new IllegalArgumentException("Student " + student.getStudentId() + " already assigned to another classroom");
            }
            student.setClassroom(classroom);
        }
        studentRepository.saveAll(students);
    }

    @Transactional
    public void removeStudentFromClassroom(Long classroomId, Long studentId) {

        // Check classroom exists
        Classroom classroom = classroomRepository.findByIdAndDeletedFalse(classroomId)
                        .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));

        // Find student in this classroom
        Student student = studentRepository.findByIdAndClassroomId(studentId, classroomId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found in this classroom"));

        // Remove relationship
        student.setClassroom(null);
        studentRepository.save(student);
    }

}
