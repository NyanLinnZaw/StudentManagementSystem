package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ClassroomRequest;
import com.example.student_management_system.dto.ClassroomResponse;
import com.example.student_management_system.service.ClassroomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ClassroomResponse> createClassroom(@Valid @RequestBody ClassroomRequest request){
        ClassroomResponse response = classroomService.createClassroom(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ClassroomResponse> updateClassroom(@PathVariable Long id,
                                                             @Valid @RequestBody ClassroomRequest request){
        ClassroomResponse response = classroomService.updateClassroom(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteClassroom(@PathVariable Long id){
        classroomService.deleteClassroom(id);
        return ResponseEntity.ok("Classroom deleted successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassroomResponse> getClassroomById(@PathVariable Long id){
        ClassroomResponse response = classroomService.getClassroomById(id);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<Page<ClassroomResponse>> getAllClassrooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ){
        Page<ClassroomResponse> response = classroomService.getAllClassrooms(
                keyword, page, size, sortBy, sortDir
                );
        return ResponseEntity.ok(response);
    }
}
