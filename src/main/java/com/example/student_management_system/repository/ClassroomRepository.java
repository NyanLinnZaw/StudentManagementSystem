package com.example.student_management_system.repository;

import com.example.student_management_system.entity.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    Optional<Classroom> findByIdAndDeletedFalse(Long id);

    Page<Classroom> findByDeletedFalse(Pageable pageable);

    boolean existsByClassroomCode(String classroomCode);

    long countByDeletedFalse();

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

    @Query("""
        SELECT c.id, c.classroomName, COUNT(s.id)
        FROM Classroom c
        LEFT JOIN Student s
        ON s.classroom.id = c.id
        AND s.deleted = false
        WHERE c.deleted = false
        GROUP BY c.id, c.classroomName
    """)
    List<Object[]> countStudentsByClassroom();
}
