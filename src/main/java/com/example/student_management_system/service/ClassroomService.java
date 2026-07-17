package com.example.student_management_system.service;

import com.example.student_management_system.dto.ClassroomRequest;
import com.example.student_management_system.dto.ClassroomResponse;
import com.example.student_management_system.entity.Classroom;
import com.example.student_management_system.exception.ResourceNotFoundException;
import com.example.student_management_system.repository.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;

    public ClassroomResponse createClassroom(ClassroomRequest request) {

        if (classroomRepository.existsByClassroomCode(request.getClassroomCode())) {
            throw new IllegalArgumentException("Classroom code already exists");
        }

        Classroom classroom = new Classroom();
        classroom.setClassroomCode(request.getClassroomCode());
        classroom.setClassroomName(request.getClassroomName());
        classroom.setCapacity(request.getCapacity());
        classroom.setDeleted(false);

        Classroom saved = classroomRepository.save(classroom);

        return mapToResponse(saved);
    }

    public ClassroomResponse updateClassroom(Long id, ClassroomRequest request) {

        Classroom classroom = classroomRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Classroom not found"));

        classroom.setClassroomCode(request.getClassroomCode());
        classroom.setClassroomName(request.getClassroomName());
        classroom.setCapacity(request.getCapacity());

        Classroom updated = classroomRepository.save(classroom);

        return mapToResponse(updated);
    }

    public void deleteClassroom(Long id) {

        Classroom classroom = classroomRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Classroom not found"));

        if (classroom.getStudents() != null &&
                !classroom.getStudents().isEmpty()) {

            throw new IllegalArgumentException(
                    "Cannot delete classroom because students are assigned."
            );
        }

        classroom.setDeleted(true);

        classroomRepository.save(classroom);
    }

    public ClassroomResponse getClassroomById(Long id) {

        Classroom classroom = classroomRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Classroom not found"));

        return mapToResponse(classroom);
    }

    public Page<ClassroomResponse> getAllClassrooms(String keyword, int page, int size,
                                                    String sortBy, String sortDir){

        Sort sort = Sort.by("id").ascending();
        if(sortBy != null && !sortBy.isBlank()){
            if("desc".equalsIgnoreCase(sortDir)){
                sort = Sort.by(sortBy).descending();
            }else{
                sort = Sort.by(sortBy).ascending();
            }
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Classroom> classrooms = classroomRepository.search(keyword, pageable);
        return classrooms.map(this::mapToResponse);
    }

    private ClassroomResponse mapToResponse(Classroom classroom) {

        int totalStudents = 0;

        if (classroom.getStudents() != null) {
            totalStudents = classroom.getStudents().size();
        }

        return ClassroomResponse.builder()
                .id(classroom.getId())
                .classroomCode(classroom.getClassroomCode())
                .classroomName(classroom.getClassroomName())
                .capacity(classroom.getCapacity())
                .totalStudents(totalStudents)
                .build();
    }

}
