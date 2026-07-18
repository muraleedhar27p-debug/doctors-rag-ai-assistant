package com.spring.ai.rag.controller;

import com.spring.ai.rag.model.Doctor;
import com.spring.ai.rag.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/departments")
    public List<String> getDepartments() {
        return doctorService.getAllDepartments();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
            "total", doctorService.getTotalCount(),
            "byDepartment", doctorService.getDepartmentStats()
        ));
    }
}
