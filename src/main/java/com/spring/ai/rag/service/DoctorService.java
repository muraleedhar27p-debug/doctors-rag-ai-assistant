package com.spring.ai.rag.service;

import com.spring.ai.rag.model.Doctor;
import com.spring.ai.rag.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public List<String> getAllDepartments() {
        return doctorRepository.findAllDepartments();
    }

    public Map<String, Long> getDepartmentStats() {
        return doctorRepository.findAll().stream()
            .collect(Collectors.groupingBy(Doctor::getDepartment, Collectors.counting()));
    }

    public long getTotalCount() {
        return doctorRepository.count();
    }
}
