package com.example.student_management_system.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;


@Data
public class AssignStudentsRequest {


    @NotEmpty(message = "Student IDs cannot be empty")
    private List<Long> studentIds;


}
