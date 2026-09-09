package com.example.student_management_system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classrooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String classroomCode;

    @Column(nullable = false)
    private String classroomName;

    private Integer capacity;

    @OneToMany(mappedBy = "classroom")
    @SQLRestriction("deleted = false")
    @Builder.Default
    private List<Student> students = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}
