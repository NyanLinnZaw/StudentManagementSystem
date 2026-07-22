package com.example.student_management_system.repository;

import com.example.student_management_system.entity.Score;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
    SELECT s
    FROM Score s
    JOIN s.student st
    JOIN s.subject sub
    WHERE s.deleted = false
      AND (
            :keyword IS NULL
         OR LOWER(st.studentId) LIKE LOWER(CONCAT('%', :keyword, '%'))
         OR LOWER(st.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
         OR LOWER(sub.subjectCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
         OR LOWER(sub.subjectName) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
""")
    Page<Score> search(@Param("keyword") String keyword,
                       Pageable pageable);

}
