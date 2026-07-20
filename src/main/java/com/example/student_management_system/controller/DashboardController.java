package com.example.student_management_system.controller;

import com.example.student_management_system.dto.DashboardResponse;
import com.example.student_management_system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<DashboardResponse> getDashboard(){
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}
