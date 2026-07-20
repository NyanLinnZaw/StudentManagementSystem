package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ScoreRequest;
import com.example.student_management_system.dto.ScoreResponse;
import com.example.student_management_system.dto.StudentScoreReportResponse;
import com.example.student_management_system.service.ScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ScoreResponse> createScore(@Valid @RequestBody ScoreRequest request) {
        ScoreResponse response = scoreService.createScore(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ScoreResponse> updateScore(@PathVariable Long id, @Valid @RequestBody ScoreRequest request) {
        ScoreResponse response = scoreService.updateScore(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<String> deleteScore(@PathVariable Long id) {
        scoreService.deleteScore(id);
        return ResponseEntity.ok("Score deleted successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ScoreResponse> getScoreById(@PathVariable Long id) {
        ScoreResponse response = scoreService.getScoreById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<StudentScoreReportResponse> getStudentReport(@PathVariable Long studentId) {
        StudentScoreReportResponse response = scoreService.getStudentReport(studentId);
        return ResponseEntity.ok(response);
    }
}
