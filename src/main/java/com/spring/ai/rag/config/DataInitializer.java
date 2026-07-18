package com.spring.ai.rag.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.ai.rag.model.Doctor;
import com.spring.ai.rag.repository.DoctorRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    /** Classpath pattern matching every JSON file in the data directory. */
    private static final String DATA_FILES_PATTERN = "classpath:data/*.json";
    private static final Pattern YEARS_PATTERN = Pattern.compile("(\\d+)");

    private final DoctorRepository doctorRepository;

    @Autowired
    private final VectorStore vectorStore;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(String... args) throws Exception {
        log.info("Seeding H2 database from all files matching {}...", DATA_FILES_PATTERN);

        List<DoctorsJson> jsonDoctors = loadDoctorsFromJson();
        List<Doctor> doctors = jsonDoctors.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());

        doctorRepository.saveAll(doctors);
        log.info("Seeded {} doctor records into H2 database.", doctors.size());
        indexDoctors();
    }

    /**
     * Discovers every *.json file under src/main/resources/data/ on the
     * classpath and merges their doctor records into a single list. Each
     * file is expected to be a JSON array with the same shape (see
     * DoctorsJson). A missing/unreadable/malformed file is logged and
     * skipped rather than failing the whole startup.
     */
    private List<DoctorsJson> loadDoctorsFromJson() {
        List<DoctorsJson> allDoctors = new ArrayList<>();

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources;
        try {
            resources = resolver.getResources(DATA_FILES_PATTERN);
        } catch (Exception e) {
            log.error("Could not scan {} for data files: {}", DATA_FILES_PATTERN, e.getMessage());
            return allDoctors;
        }

        // Sort by filename so seeding order is deterministic across runs.
        Arrays.sort(resources, (a, b) -> a.getFilename().compareTo(b.getFilename()));

        log.info("Discovered {} data file(s) under data/: {}", resources.length,
                Arrays.stream(resources).map(Resource::getFilename).collect(Collectors.joining(", ")));

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            try (InputStream is = resource.getInputStream()) {
                List<DoctorsJson> doctorsFromFile = objectMapper.readValue(
                        is,
                        objectMapper.getTypeFactory()
                                .constructCollectionType(List.class, DoctorsJson.class)
                );
                log.info("Loaded {} doctor records from {}.", doctorsFromFile.size(), filename);
                allDoctors.addAll(doctorsFromFile);
            } catch (Exception e) {
                log.warn("Skipping data file {} - could not load it: {}", filename, e.getMessage());
            }
        }

        return allDoctors;
    }

    private Doctor toEntity(DoctorsJson j) {
        boolean isChief = j.role != null && (
                j.role.toLowerCase(Locale.ROOT).contains("chief")
                        || j.role.toLowerCase(Locale.ROOT).contains("chair")
                        || j.role.toLowerCase(Locale.ROOT).contains("director")
        );

        String email = j.fullName == null ? null : j.fullName
                .replace("Dr. ", "")
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace(" ", ".")
                + "@hospital.com";

        return Doctor.builder()
                .name(j.fullName)
                .age(j.age)
                .gender(j.gender)
                .education(j.education)
                .department(j.department)
                .role(j.role)
                .email(email)
                .phone(j.phoneNumber)
                .yearsExperience(parseYears(j.experience))
                .skills(join(j.skills))
                .clinicalSkills(join(j.clinicalSkills))
                .specialization(j.specialization)
                .certificates(join(j.certificates))
                .degreesWithUniversities(j.degreesWithUniversities)
                .salaryBand(isChief ? "Chief" : "Attending")
                .location(j.currentLocation)
                .managerName(isChief ? "Medical Director" : "Department Chief")
                .rating(j.ratings)
                .reviewCount(j.reviewCount)
                .reviews(join(j.reviews))
                .spokenLanguages(join(j.spokenLanguages))
                .fee(j.fee)
                .workingHospitalName(j.workingHospitalName)
                .photoUrl(j.photoUrl)
                .bio(buildBio(j))
                .build();
    }

    private String buildBio(DoctorsJson j) {
        String topSkill = (j.clinicalSkills != null && !j.clinicalSkills.isEmpty())
                ? j.clinicalSkills.get(0) : "general medicine";
        return String.format(
                "%s is a %s in %s based in %s, with %s of experience specializing in %s. " +
                        "Fluent in %s, %s practices at %s and holds a %.1f-star patient rating from %d reviews.",
                j.fullName, j.role, j.department, j.currentLocation, j.experience,
                j.specialization != null ? j.specialization.toLowerCase(Locale.ROOT) : topSkill.toLowerCase(Locale.ROOT),
                j.spokenLanguages != null ? String.join(", ", j.spokenLanguages) : "English",
                j.fullName != null && j.fullName.contains(" ") ? j.fullName.split(" ")[1] : j.fullName,
                j.workingHospitalName,
                j.ratings != null ? j.ratings : 0.0,
                j.reviewCount != null ? j.reviewCount : 0
        );
    }

    private Integer parseYears(String experience) {
        if (experience == null) return null;
        Matcher m = YEARS_PATTERN.matcher(experience);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }

    private String join(List<String> items) {
        return items == null ? null : String.join("; ", items);
    }

    public void indexDoctors() {
        log.info("Starting doctor indexing into vector store...");
        List<Doctor> doctors = doctorRepository.findAll();

        List<Document> documents = doctors.stream()
                .map(doc -> new Document(
                        doc.toDocument(),
                        Map.of(
                                "doctorId", doc.getId().toString(),
                                "name", doc.getName(),
                                "department", doc.getDepartment(),
                                "role", doc.getRole(),
                                "specialization", doc.getSpecialization() != null ? doc.getSpecialization() : "",
                                "location", doc.getLocation() != null ? doc.getLocation() : ""
                        )
                ))
                .collect(Collectors.toList());

        vectorStore.add(documents);
        log.info("Indexed {} doctor documents into vector store.", documents.size());
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DoctorsJson {
        private String fullName;
        private Integer age;
        private String experience;
        private String gender;
        private String education;
        private String department;
        private String role;
        private List<String> skills;
        private String specialization;
        private List<String> certificates;
        private String degreesWithUniversities;
        private Double ratings;
        private List<String> reviews;
        private Integer reviewCount;
        private String phoneNumber;
        private List<String> clinicalSkills;
        private List<String> spokenLanguages;
        private String currentLocation;
        private String fee;
        private String workingHospitalName;
        private String photoUrl;
    }
}