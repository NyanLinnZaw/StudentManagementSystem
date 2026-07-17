package com.example.student_management_system.repository;

import com.example.student_management_system.entity.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    Optional<Classroom> findByIdAndDeletedFalse(Long id);

    Page<Classroom> findByDeletedFalse(Pageable pageable);

    boolean existsByClassroomCode(String classroomCode);

    @Query("""
        SELECT c 
        FROM Classroom c
        WHERE c.deleted = false
        AND (
            :keyword IS NULL 
            OR LOWER(c.classroomCode)
                LIKE LOWER(CONCAT('%',:keyword,'%'))
            OR LOWER(c.classroomName)
                LIKE LOWER(CONCAT('%',:keyword,'%'))
        )
    """)
    Page<Classroom> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
