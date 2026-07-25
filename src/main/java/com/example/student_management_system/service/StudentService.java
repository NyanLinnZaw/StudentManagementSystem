package com.example.student_management_system.service;

import com.example.student_management_system.dto.StudentRequest;
import com.example.student_management_system.dto.StudentResponse;
import com.example.student_management_system.entity.Classroom;
import com.example.student_management_system.entity.Student;
import com.example.student_management_system.entity.User;
import com.example.student_management_system.enums.RoleName;
import com.example.student_management_system.exception.ClassroomCapacityException;
import com.example.student_management_system.exception.ResourceNotFoundException;
import com.example.student_management_system.repository.ClassroomRepository;
import com.example.student_management_system.repository.StudentRepository;
import com.example.student_management_system.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    public StudentService(StudentRepository studentRepository,
                          ClassroomRepository classroomRepository,
                          UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;
    }

    public StudentResponse createStudent(StudentRequest request) {
        if (request.getStudentId() == null || request.getStudentId().isBlank()) {
            throw new IllegalArgumentException("Student ID is required");
        }

        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new IllegalArgumentException("Student ID already exists");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && studentRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.getRole().getName().equals(RoleName.STUDENT)) {
            throw new IllegalArgumentException(
                    "User role must be STUDENT"
            );
        }

        Student student = new Student();
        student.setUser(user);
        student.setStudentId(request.getStudentId());
        student.setFullName(request.getFullName());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setGender(request.getGender());
        student.setEmail(request.getEmail());
        student.setAddress(request.getAddress());
        student.setParentPhone(request.getParentPhone());
        //student.setAvatar(request.getAvatar());
        student.setDeleted(false);

        if (request.getEnrollmentDate() != null) {
            student.setEnrollmentDate(request.getEnrollmentDate());
        } else {
            student.setEnrollmentDate(LocalDate.now());
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            student.setStatus(request.getStatus());
        } else {
            student.setStatus("ACTIVE");
        }

        if (request.getClassroomId() != null) {
            Classroom classroom = classroomRepository.findById(request.getClassroomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));
            int currentStudentCount = classroom.getStudents().size();
            if(currentStudentCount >= classroom.getCapacity()) {
                throw new ClassroomCapacityException("Classroom capacity is full. Maximum capacity is " + classroom.getCapacity());
            }
            student.setClassroom(classroom);
        }

        Student saved = studentRepository.save(student);
        return mapToResponse(saved);
    }

    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = studentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && studentRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new IllegalArgumentException("Email already exists");
        }

        student.setFullName(request.getFullName());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setGender(request.getGender());
        student.setEmail(request.getEmail());
        student.setAddress(request.getAddress());
        student.setParentPhone(request.getParentPhone());
        student.setEnrollmentDate(request.getEnrollmentDate());
        //student.setAvatar(request.getAvatar());

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            student.setStatus(request.getStatus());
        }

        if (request.getClassroomId() != null) {
            Classroom classroom = classroomRepository.findById(request.getClassroomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));
            student.setClassroom(classroom);
        } else {
            student.setClassroom(null);
        }

        Student updated = studentRepository.save(student);
        return mapToResponse(updated);
    }

    public void deleteStudent(Long id) {
        Student student = studentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        student.setDeleted(true);
        student.setStatus("DELETED");
        studentRepository.save(student);
    }

    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return mapToResponse(student);
    }

    public Page<StudentResponse> getAllStudents(String keyword, Long classroomId,
                                                int page, int size,
                                                String sortBy, String sortDir) {
        Sort sort = Sort.by("id").ascending();
        if (sortBy != null && !sortBy.isBlank()) {
            if ("desc".equalsIgnoreCase(sortDir)) {
                sort = Sort.by(sortBy).descending();
            } else {
                sort = Sort.by(sortBy).ascending();
            }
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Student> students = studentRepository.search(keyword, classroomId, pageable);
        return students.map(this::mapToResponse);
    }

    private StudentResponse mapToResponse(Student student) {
        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setStudentId(student.getStudentId());
        response.setFullName(student.getFullName());
        response.setDateOfBirth(student.getDateOfBirth());
        response.setGender(student.getGender());
        response.setEmail(student.getEmail());
        response.setAddress(student.getAddress());
        response.setParentPhone(student.getParentPhone());
        response.setEnrollmentDate(student.getEnrollmentDate());
        response.setStatus(student.getStatus());
        //response.setAvatar(student.getAvatar());

        if (student.getClassroom() != null) {
            response.setClassroomId(student.getClassroom().getId());
            response.setClassroomCode(student.getClassroom().getClassroomCode());
            response.setClassroomName(student.getClassroom().getClassroomName());
        }

        return response;
    }
}
