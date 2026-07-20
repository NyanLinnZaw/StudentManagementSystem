package com.example.student_management_system.service;

import com.example.student_management_system.dto.ScoreRequest;
import com.example.student_management_system.dto.ScoreResponse;
import com.example.student_management_system.dto.StudentScoreReportResponse;
import com.example.student_management_system.entity.Score;
import com.example.student_management_system.entity.Student;
import com.example.student_management_system.entity.Subject;
import com.example.student_management_system.exception.ResourceNotFoundException;
import com.example.student_management_system.repository.ScoreRepository;
import com.example.student_management_system.repository.StudentRepository;
import com.example.student_management_system.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    // Create Score
    public ScoreResponse createScore(ScoreRequest request) {
        boolean exists = scoreRepository.existsByStudentIdAndSubjectId(
                        request.getStudentId(),
                        request.getSubjectId());
        if(exists){
            throw new IllegalArgumentException("Score already exists for this student and subject");
        }
        Student student = studentRepository.findById(request.getStudentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                        .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        Score score = new Score();
        score.setStudent(student);
        score.setSubject(subject);
        score.setAssignmentScore(request.getAssignmentScore());
        score.setMidtermScore(request.getMidtermScore());
        score.setFinalScore(request.getFinalScore());
        calculateScore(score);
        score.setDeleted(false);
        Score saved = scoreRepository.save(score);
        return mapToResponse(saved);
    }

    // Update Score
    public ScoreResponse updateScore(Long id, ScoreRequest request){
        Score score = scoreRepository.findByIdAndDeletedFalse(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Score not found"));

        score.setAssignmentScore(request.getAssignmentScore());
        score.setMidtermScore(request.getMidtermScore());
        score.setFinalScore(request.getFinalScore());
        calculateScore(score);
        Score updated = scoreRepository.save(score);
        return mapToResponse(updated);
    }

    // Soft Delete
    public void deleteScore(Long id){
        Score score = scoreRepository.findByIdAndDeletedFalse(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Score not found"));
        score.setDeleted(true);
        scoreRepository.save(score);
    }

    // Get Score Detail
    public ScoreResponse getScoreById(Long id){
        Score score = scoreRepository.findByIdAndDeletedFalse(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Score not found"));
        return mapToResponse(score);
    }

    // Student Score Report
    public StudentScoreReportResponse getStudentReport(Long studentId){
        Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        List<Score> scores = scoreRepository.findByStudentIdAndDeletedFalse(studentId);
        List<ScoreResponse> scoreResponses = scores.stream()
                        .map(this::mapToResponse)
                        .toList();
        double overallAverage = scores.stream()
                        .mapToDouble(Score::getAverageScore)
                        .average()
                        .orElse(0);
        double overallGpa = calculateGpa(overallAverage);
        return StudentScoreReportResponse.builder().studentId(student.getId()).studentCode(
                        student.getStudentId())
                .studentName(student.getFullName())
                .classroomName(student.getClassroom() != null ? student.getClassroom().getClassroomName() : null)
                .scores(scoreResponses)
                .overallAverage(round(overallAverage))
                .overallGpa(overallGpa)
                .build();
    }

    // Calculate Average + GPA
    private void calculateScore(Score score){
        double average = (score.getAssignmentScore() + score.getMidtermScore() + score.getFinalScore()) / 3;
        score.setAverageScore(round(average));
        score.setGpa(calculateGpa(average));
    }

    private Double calculateGpa(double average){
        if(average >= 9)
            return 4.0;
        if(average >= 8)
            return 3.5;
        if(average >= 7)
            return 3.0;
        if(average >= 6)
            return 2.5;
        if(average >= 5)
            return 2.0;
        return 0.0;
    }

    // Entity -> Response
    private ScoreResponse mapToResponse(Score score){
        return ScoreResponse.builder()
                .id(score.getId())
                .studentId(score.getStudent().getId())
                .studentName(score.getStudent().getFullName())
                .subjectId(score.getSubject().getId())
                .subjectCode(score.getSubject().getSubjectCode())
                .subjectName(score.getSubject().getSubjectName())
                .assignmentScore(score.getAssignmentScore())
                .midtermScore(score.getMidtermScore())
                .finalScore(score.getFinalScore())
                .averageScore(score.getAverageScore())
                .gpa(score.getGpa())
                .build();
    }

    private Double round(Double value){
        return Math.round(value * 100.0) / 100.0;
    }
}
