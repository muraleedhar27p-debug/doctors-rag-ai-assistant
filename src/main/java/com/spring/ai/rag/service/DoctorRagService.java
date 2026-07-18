package com.spring.ai.rag.service;

import com.spring.ai.rag.model.Doctor;
import com.spring.ai.rag.repository.DoctorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
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


    public DoctorRagService(DoctorRepository doctorRepository, VectorStore vectorStore, ChatClient chatClient) {
        this.doctorRepository = doctorRepository;
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }

    /**
     * RAG pipeline:
     *  1. Semantic search on the vector store with the user query.
     *  2. Build a grounded prompt with the retrieved doctor context.
     *  3. Send to Claude and return the answer.
     */
    public String query(String userQuestion) {
        log.info("RAG query: {}", userQuestion);

        // Step 1 – retrieve top-k relevant documents
        List<Document> relevant = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(userQuestion)
                .topK(5)
                .build()
        );

        if (relevant.isEmpty()) {
            return "I could not find any relevant doctor information for your question.";
        }

        // Step 2 – assemble context
        String context = relevant.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n---\n\n"));

        // Step 3 – call Claude with retrieved context
        String systemPrompt = """
            You are an intelligent Doctor assistant with access to the hospital doctor directory.
            Use ONLY the doctor data provided in the CONTEXT section to answer the user's question.
            Be concise, accurate, and helpful.
            If the answer is not in the context, say so honestly instead of guessing.
            Format lists and tables clearly when presenting multiple doctors.
            """;

        String userPrompt = String.format("""
            CONTEXT (retrieved doctor records):
            %s

            USER QUESTION:
            %s
            """, context, userQuestion);

        String answer = chatClient.prompt()
            .system(systemPrompt)
            .user(userPrompt)
            .call()
            .content();

        log.info("RAG answer generated ({} chars)", answer.length());
        return answer;
    }

    /**
     * Re-index all doctor (useful if data changes at runtime).
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
                    "role",       doctor.getRole()
                )
            ))
            .collect(Collectors.toList());

        vectorStore.add(documents);
        log.info("Re-indexed {} documents.", documents.size());
        return documents.size();
    }
}
