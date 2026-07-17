package com.example.student_management_system.repository;

import com.example.student_management_system.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByIdAndDeletedFalse(Long id);

    boolean existsBySubjectCode(String subjectCode);

    @Query("""
        SELECT s
        FROM Subject s
        WHERE s.deleted = false
        AND (
            :keyword IS NULL
            OR LOWER(s.subjectCode)
               LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(s.subjectName)
               LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    """)
    Page<Subject> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );

}
