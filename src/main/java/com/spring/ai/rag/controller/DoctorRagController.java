package com.spring.ai.rag.controller;

import com.spring.ai.rag.service.DoctorRagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/rag")
//@CrossOrigin(origins = "*")
public class DoctorRagController {

    private final DoctorRagService doctorRagService;

    public DoctorRagController(DoctorRagService doctorRagService) {
        this.doctorRagService = doctorRagService;
    }

    /**
     * POST /api/rag/query
     * Body: { "question": "Who are the engineers in San Francisco?" }
     */
    @PostMapping("/query")
    public ResponseEntity<Map<String, String>> query(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Question must not be empty"));
        }
        String answer = doctorRagService.query(question.trim());
        return ResponseEntity.ok(Map.of("answer", answer));
    }

    /**
     * POST /api/rag/reindex
     * Re-indexes all employees into the vector store.
     */
    @PostMapping("/reindex")
    public ResponseEntity<Map<String, Object>> reindex() {
        int count = doctorRagService.reindex();
        return ResponseEntity.ok(Map.of(
            "message", "Re-indexed successfully",
            "documentsIndexed", count
        ));
    }
}
