package com.example.student_management_system.service;

import com.example.student_management_system.dto.SubjectRequest;
import com.example.student_management_system.dto.SubjectResponse;
import com.example.student_management_system.entity.Subject;
import com.example.student_management_system.exception.ResourceNotFoundException;
import com.example.student_management_system.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectResponse createSubject(SubjectRequest request) {

        if (subjectRepository.existsBySubjectCode(request.getSubjectCode())) {
            throw new IllegalArgumentException("Subject code already exists");
        }

        Subject subject = new Subject();
        subject.setSubjectCode(request.getSubjectCode());
        subject.setSubjectName(request.getSubjectName());
        subject.setDescription(request.getDescription());
        subject.setDeleted(false);

        return mapToResponse(subjectRepository.save(subject));
    }

    public SubjectResponse updateSubject(Long id, SubjectRequest request) {

        Subject subject = subjectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Subject not found"));

        subject.setSubjectCode(request.getSubjectCode());
        subject.setSubjectName(request.getSubjectName());
        subject.setDescription(request.getDescription());

        return mapToResponse(subjectRepository.save(subject));
    }

    public void deleteSubject(Long id) {

        Subject subject = subjectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Subject not found"));

        subject.setDeleted(true);

        subjectRepository.save(subject);
    }

    public SubjectResponse getSubjectById(Long id) {

        Subject subject = subjectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Subject not found"));

        return mapToResponse(subject);
    }

    public Page<SubjectResponse> getAllSubjects(
            String keyword,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {

        Sort sort = Sort.by("id").ascending();

        if (sortBy != null && !sortBy.isBlank()) {
            sort = "desc".equalsIgnoreCase(sortDir)
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        return subjectRepository.search(keyword, pageable)
                .map(this::mapToResponse);
    }

    private SubjectResponse mapToResponse(Subject subject) {

        return SubjectResponse.builder()
                .id(subject.getId())
                .subjectCode(subject.getSubjectCode())
                .subjectName(subject.getSubjectName())
                .description(subject.getDescription())
                .build();
    }

}
