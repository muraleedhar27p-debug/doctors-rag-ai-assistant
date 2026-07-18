package com.spring.ai.rag.repository;

import com.spring.ai.rag.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByDepartmentIgnoreCase(String department);

    List<Doctor> findByRoleContainingIgnoreCase(String role);

    @Query("SELECT DISTINCT e.department FROM Doctor e ORDER BY e.department")
    List<String> findAllDepartments();

    @Query("SELECT e FROM Doctor e WHERE e.yearsExperience >= :minYears")
    List<Doctor> findByMinimumExperience(int minYears);
}
