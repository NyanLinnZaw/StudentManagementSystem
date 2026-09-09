package com.example.student_management_system.service;

import com.example.student_management_system.dto.StudentRequest;
import com.example.student_management_system.dto.StudentResponse;
import com.example.student_management_system.entity.Classroom;
import com.example.student_management_system.entity.Student;
import com.example.student_management_system.entity.User;
import com.example.student_management_system.enums.StudentStatus;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

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

        StudentStatus status = parseStatus(request.getStatus());
        if (status == StudentStatus.INACTIVE && request.getClassroomId() != null) {
            throw new IllegalArgumentException("Inactive students cannot be assigned to a classroom");
        }

        Student student = new Student();
        student.setStudentId(request.getStudentId());
        student.setFullName(request.getFullName());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setGender(request.getGender());
        student.setEmail(request.getEmail());
        student.setAddress(request.getAddress());
        student.setParentPhone(request.getParentPhone());
        student.setDeleted(false);
        student.setStatus(status);

        if (request.getEnrollmentDate() != null) {
            student.setEnrollmentDate(request.getEnrollmentDate());
        } else {
            student.setEnrollmentDate(LocalDate.now());
        }

        if (request.getClassroomId() != null) {
            Classroom classroom = classroomRepository.findById(request.getClassroomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));
            int currentStudentCount = classroom.getStudents().size();
            if (currentStudentCount >= classroom.getCapacity()) {
                throw new ClassroomCapacityException(
                        "Classroom capacity is full. Maximum capacity is " + classroom.getCapacity());
            }
            student.setClassroom(classroom);
        }

        Student saved = studentRepository.save(student);
        return mapToResponse(saved);
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = studentRepository.findByIdAndDeletedFalseWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && studentRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new IllegalArgumentException("Email already exists");
        }

        StudentStatus previousStatus = student.getStatus();
        StudentStatus newStatus = request.getStatus() != null && !request.getStatus().isBlank()
                ? parseStatus(request.getStatus())
                : student.getStatus();

        if (newStatus == StudentStatus.INACTIVE && request.getClassroomId() != null) {
            Long currentClassroomId = student.getClassroom() != null
                    ? student.getClassroom().getId()
                    : null;
            if (!Objects.equals(currentClassroomId, request.getClassroomId())) {
                throw new IllegalArgumentException("Cannot change classroom for inactive student");
            }
        }

        student.setFullName(request.getFullName());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setGender(request.getGender());
        student.setEmail(request.getEmail());
        student.setAddress(request.getAddress());
        student.setParentPhone(request.getParentPhone());
        student.setEnrollmentDate(request.getEnrollmentDate());
        student.setStatus(newStatus);

        if (student.getStatus() == StudentStatus.ACTIVE) {
            if (request.getClassroomId() != null) {
                Classroom classroom = classroomRepository.findById(request.getClassroomId())
                        .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));
                student.setClassroom(classroom);
            } else {
                student.setClassroom(null);
            }
        }

        if (previousStatus != newStatus) {
            syncUserEnabled(student, newStatus == StudentStatus.ACTIVE);
        }

        Student updated = studentRepository.save(student);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findByIdAndDeletedFalseWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        student.setDeleted(true);
        syncUserEnabled(student, false);
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

    public static void requireActive(Student student) {
        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw new IllegalArgumentException("Only active students can perform this action");
        }
    }

    private StudentStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return StudentStatus.ACTIVE;
        }
        try {
            return StudentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status. Allowed values: ACTIVE, INACTIVE");
        }
    }

    private void syncUserEnabled(Student student, boolean enabled) {
        User user = student.getUser();
        if (user != null) {
            user.setEnabled(enabled);
            userRepository.save(user);
        }
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
        response.setStatus(student.getStatus().name());

        if (student.getClassroom() != null) {
            response.setClassroomId(student.getClassroom().getId());
            response.setClassroomCode(student.getClassroom().getClassroomCode());
            response.setClassroomName(student.getClassroom().getClassroomName());
        }

        if (student.getUser() != null) {
            response.setUserId(student.getUser().getId());
        }

        return response;
    }
}
