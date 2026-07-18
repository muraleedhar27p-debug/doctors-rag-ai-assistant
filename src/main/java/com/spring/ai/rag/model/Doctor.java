package com.spring.ai.rag.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doctors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    private String skills;

    @Column(name = "salary_band")
    private String salaryBand;

    private String location;

    @Column(name = "manager_name")
    private String managerName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    /**
     * Converts employee data into a rich text document for vector indexing.
     */
    public String toDocument() {
        return String.format(
            "Doctor Profile:\n" +
            "Name: %s\n" +
            "Department: %s\n" +
            "Role: %s\n" +
            "Email: %s\n" +
            "Phone: %s\n" +
            "Years of Experience: %d\n" +
            "Skills: %s\n" +
            "Salary Band: %s\n" +
            "Location: %s\n" +
            "Manager: %s\n" +
            "Bio: %s",
            name, department, role, email, phone,
            yearsExperience, skills, salaryBand, location, managerName, bio
        );
    }
}
