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

    private Integer age;

    private String gender;

    @Column(columnDefinition = "TEXT")
    private String education;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String role;

    private String email;

    private String phone;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String clinicalSkills;

    private String specialization;

    @Column(columnDefinition = "TEXT")
    private String certificates;

    @Column(columnDefinition = "TEXT")
    private String degreesWithUniversities;

    @Column(name = "salary_band")
    private String salaryBand;

    private String location;

    @Column(name = "manager_name")
    private String managerName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private Double rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(columnDefinition = "TEXT")
    private String reviews;

    private String spokenLanguages;

    private String fee;

    private String workingHospitalName;

    private String photoUrl;

    /**
     * Converts doctor data into a rich text document for vector indexing.
     */
    public String toDocument() {
        return String.format(
            "Doctor Profile:\n" +
            "Name: %s\n" +
            "Age: %s\n" +
            "Gender: %s\n" +
            "Education: %s\n" +
            "Department: %s\n" +
            "Role: %s\n" +
            "Email: %s\n" +
            "Phone: %s\n" +
            "Years of Experience: %s\n" +
            "Skills: %s\n" +
            "Clinical Skills: %s\n" +
            "Specialization: %s\n" +
            "Certificates: %s\n" +
            "Degrees with Universities: %s\n" +
            "Salary Band: %s\n" +
            "Location: %s\n" +
            "Manager: %s\n" +
            "Rating: %s\n" +
            "Review Count: %s\n" +
            "Reviews: %s\n" +
            "Spoken Languages: %s\n" +
            "Consultation Fee: %s\n" +
            "Working Hospital: %s\n" +
            "Photo URL: %s\n" +
            "Bio: %s",
            name, age, gender, education, department, role, email, phone,
            yearsExperience, skills, clinicalSkills, specialization, certificates,
            degreesWithUniversities, salaryBand, location, managerName, rating,
            reviewCount, reviews, spokenLanguages, fee, workingHospitalName, photoUrl, bio
        );
    }
}
