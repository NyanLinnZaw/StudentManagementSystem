package com.example.student_management_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subjects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String subjectCode;

    @Column(nullable = false)
    private String subjectName;

    private String description;

    @OneToMany(mappedBy = "subject")
    @Builder.Default
    private List<Score> scores = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}
