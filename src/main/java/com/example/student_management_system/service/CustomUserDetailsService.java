package com.example.student_management_system.service;

import com.example.student_management_system.entity.User;
import com.example.student_management_system.enums.RoleName;
import com.example.student_management_system.enums.StudentStatus;
import com.example.student_management_system.repository.StudentRepository;
import com.example.student_management_system.repository.UserRepository;
import com.example.student_management_system.security.CustomUserDetails;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    public static final String INACTIVE_STUDENT_MESSAGE = "Student account is inactive";

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public CustomUserDetailsService(UserRepository userRepository,
                                      StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getRole().getName() == RoleName.STUDENT) {
            studentRepository.findByUser_Id(user.getId()).ifPresent(student -> {
                if (student.getStatus() == StudentStatus.INACTIVE) {
                    throw new DisabledException(INACTIVE_STUDENT_MESSAGE);
                }
            });
        }

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new DisabledException("Account is disabled");
        }

        return new CustomUserDetails(user);
    }
}
