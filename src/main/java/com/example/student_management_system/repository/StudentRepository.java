package com.example.student_management_system.repository;

import com.example.student_management_system.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentId(String studentId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<Student> findByIdAndDeletedFalse(Long id);

    Page<Student> findByClassroomIdAndDeletedFalse(Long classroomId, Pageable pageable);

    long countByDeletedFalse();

    List<Student> findAllByIdIn(List<Long> ids);

    long countByClassroomIdAndDeletedFalse(Long classroomId);

    Optional<Student> findByIdAndClassroomId(Long studentId, Long classroomId);

    @Query("SELECT s FROM Student s LEFT JOIN s.classroom c " +
            "WHERE s.deleted = false " +
            "AND (:keyword IS NULL OR :keyword = '' " +
            "OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.studentId) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.classroomCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.classroomName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:classroomId IS NULL OR c.id = :classroomId)")
    Page<Student> search(@Param("keyword") String keyword,
                         @Param("classroomId") Long classroomId,
                         Pageable pageable);
}
