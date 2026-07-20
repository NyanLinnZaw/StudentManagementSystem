package com.example.student_management_system.repository;

import com.example.student_management_system.entity.Score;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    Optional<Score> findByIdAndDeletedFalse(Long id);

    boolean existsByStudentIdAndSubjectId(Long studentId,
                                          Long subjectId);

    Page<Score> findByDeletedFalse(Pageable pageable);

    List<Score> findByStudentIdAndDeletedFalse(Long studentId);

    @Query("""
        SELECT s.student.id,
               s.student.studentId,
               s.student.fullName,
               AVG(s.gpa)
        FROM Score s
        WHERE s.deleted = false
        GROUP BY s.student.id,
                 s.student.studentId,
                 s.student.fullName
        ORDER BY AVG(s.gpa) DESC
    """)
    List<Object[]> findTopStudents();

}
