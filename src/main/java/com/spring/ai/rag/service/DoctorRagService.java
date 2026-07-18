package com.spring.ai.rag.service;

import com.spring.ai.rag.model.Doctor;
import com.spring.ai.rag.repository.DoctorRepository;
import com.spring.ai.rag.tools.AppointmentTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DoctorRagService {

    @Autowired
    private final DoctorRepository doctorRepository;
    @Autowired
    private final VectorStore vectorStore;
    @Autowired
    private final ChatClient chatClient;
    @Autowired
    private final AppointmentTools appointmentTools;


    public DoctorRagService(DoctorRepository doctorRepository, VectorStore vectorStore,
                             ChatClient chatClient, AppointmentTools appointmentTools) {
        this.doctorRepository = doctorRepository;
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
        this.appointmentTools = appointmentTools;
    }

    /**
     * RAG pipeline:
     *  1. Semantic search on the vector store with the user query.
     *  2. Build a grounded prompt with the retrieved doctor context.
     *  3. Send to Claude — with appointment-booking tools available — and return the answer.
     */
    public String query(String conversationId, String userQuestion) {
        log.info("RAG query: {}", userQuestion);

        // Step 1 – retrieve top-k relevant documents
        List<Document> relevant = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(userQuestion)
                .topK(5)
                .build()
        );

        String context = relevant.isEmpty()
                ? "No matching doctor records were found for this query."
                : relevant.stream().map(Document::getText).collect(Collectors.joining("\n\n---\n\n"));

        // Step 2 – assemble grounded prompt
        String systemPrompt = """
            You are an intelligent Doctor assistant with access to the hospital doctor directory
            and appointment booking tools.

            Use ONLY the doctor data provided in the CONTEXT section to answer questions about doctors.
            Be concise, accurate, and helpful. If the answer is not in the context, say so honestly
            instead of guessing.

            APPOINTMENT BOOKING:
            If the user asks to book, schedule, or find an appointment:
              1. Identify the doctor and desired date from the conversation. If missing, ask the user.
              2. Call findAvailableSlots to check open times for that doctor and date.
              3. Present the available slots to the user and ask them to pick one, unless they already
                 specified an exact time.
              4. Before booking, make sure you have the patient's full name and email address — ask if
                 not already provided.
              5. Once you have a confirmed slot, patient name, and email, call bookAppointment.
              6. Confirm the booking result back to the user in plain language.
            Never call bookAppointment without first checking availability and collecting the patient's
            name and email.

            Format lists and tables clearly when presenting multiple doctors.
            """;

        String userPrompt = String.format("""
            CONTEXT (retrieved doctor records):
            %s

            USER QUESTION:
            %s
            """, context, userQuestion +
                " additionally, remove <thinking>, </thinking>, <answer>, </answer> tags remove from response" +
                "and show information with headings. " +
                "provide a URL to the doctor's photo if available in the context. " +
                "If no photo is available, respond that no photo is on record."
        );

        // Step 3 – call Claude with retrieved context and booking tools available
/*        String answer = chatClient.prompt()
            .system(systemPrompt)
            .user(userPrompt)
            .tools(appointmentTools)
            .call()
            .content();*/

        String answer = chatClient.prompt()
                    .advisors(advisor -> advisor.param(
                            ChatMemory.CONVERSATION_ID,
                            conversationId))
                    //.system(systemPrompt)
                    .user(userPrompt)
                    .tools(appointmentTools)
                    .call()
                    .content();

        assert answer != null;
        log.info("RAG answer generated ({} chars)", answer.length());
        return answer;
    }

    /**
     * Re-index all doctors (useful if data changes at runtime).
     */
    public int reindex() {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Document> documents = doctors.stream()
            .map(doctor -> new Document(
                    doctor.toDocument(),
                Map.of(
                    "doctorId", doctor.getId().toString(),
                    "name",       doctor.getName(),
                    "department", doctor.getDepartment(),
                    "role",       doctor.getRole(),
                    "specialization", doctor.getSpecialization() != null ? doctor.getSpecialization() : "",
                    "location",   doctor.getLocation() != null ? doctor.getLocation() : ""
                )
            ))
            .collect(Collectors.toList());

        vectorStore.add(documents);
        log.info("Re-indexed {} documents.", documents.size());
        return documents.size();
    }
}
