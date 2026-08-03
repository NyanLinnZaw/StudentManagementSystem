package com.example.student_management_system.service;

import com.example.student_management_system.dto.CreateUserRequest;
import com.example.student_management_system.dto.UserResponse;
import com.example.student_management_system.entity.Role;
import com.example.student_management_system.entity.Student;
import com.example.student_management_system.entity.User;
import com.example.student_management_system.enums.RoleName;
import com.example.student_management_system.exception.ResourceNotFoundException;
import com.example.student_management_system.repository.RoleRepository;
import com.example.student_management_system.repository.StudentRepository;
import com.example.student_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (request.getEmail() != null
                && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        RoleName roleName;
        try {
            roleName = RoleName.valueOf(request.getRole().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid role");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        Student student = null;
        if (roleName == RoleName.STUDENT) {
            if (request.getStudentCode() == null || request.getStudentCode().isBlank()) {
                throw new IllegalArgumentException("Student code is required for STUDENT account");
            }
            student = studentRepository.findByStudentIdAndDeletedFalse(request.getStudentCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Student not found with code: " + request.getStudentCode()));
            if (student.getUser() != null) {
                throw new IllegalArgumentException(
                        "This student already has an account");
            }
            if (student.getEmail() == null || student.getEmail().isBlank()) {
                throw new IllegalArgumentException(
                        "Student has no email; cannot create account");
            }
            if (request.getEmail() == null || request.getEmail().isBlank()
                    || !student.getEmail().equalsIgnoreCase(request.getEmail())) {
                throw new IllegalArgumentException(
                        "Email must match the student's email");
            }
        } else if (request.getStudentCode() != null && !request.getStudentCode().isBlank()) {
            throw new IllegalArgumentException(
                    "Student code is only allowed when role is STUDENT");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(role);
        user.setEnabled(true);

        User saved = userRepository.save(user);

        if (student != null) {
            student.setUser(saved);
            studentRepository.save(student);
        }

        return mapToResponse(saved, student);
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> mapToResponse(user, user.getStudent())).toList();
    }

    private UserResponse mapToResponse(User user, Student student) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().getName().name());
        if (student != null) {
            response.setStudentCode(student.getStudentId());
        }
        return response;
    }
}
