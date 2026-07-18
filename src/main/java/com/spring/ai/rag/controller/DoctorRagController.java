package com.spring.ai.rag.controller;

import com.spring.ai.rag.service.DoctorRagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
/*    @PostMapping("/query")
    public ResponseEntity<Map<String, String>> query(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Question must not be empty"));
        }
        String answer = doctorRagService.query("history20", question.trim());
        return ResponseEntity.ok(Map.of("answer", answer));
    }*/

/*    @PostMapping("/query")
    public ResponseEntity<Map<String, String>> query(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Question must not be empty"));
        }
        String answer = doctorRagService.query("history20", question.trim());
        System.out.println("Answer from RAG service: " + answer);
        return ResponseEntity.ok(Map.of("answer", answer));
    }*/

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

    public String getImageUrl(String photoUrl) {
        String base64Image = null;
        try {
            File imageFile = new File(photoUrl);

            if (!imageFile.exists()) {
                System.err.println("Image file not found: " + imageFile.getAbsolutePath());
                return null;
            }

            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            base64Image = Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            System.err.println("Failed to convert image to HTML: " + e.getMessage());
            e.printStackTrace();
        }
        return buildHtml(base64Image, detectMimeType(photoUrl));
    }

    private static String detectMimeType(String filePath) {
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg"))  return "image/svg+xml";
        return "application/octet-stream"; // fallback
    }

    private static String buildHtml(String base64Image, String mimeType) {
        return "\n<img id=\"embeddedImage\" src=\"data:" + mimeType + ";base64," + base64Image + "\">\n";
    }

    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> query(@RequestBody Map<String, String> request) throws IOException {
        String question = request.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Question must not be empty"));
        }

        String answer = doctorRagService.query("history20", question.trim());
        log.info("Answer from RAG service: {}", answer);

        Map<String, Object> body = new HashMap<>();
        body.put("answer", answer);

        Pattern pattern = Pattern.compile(
                "https://[^\\s\"'<>]+\\.(jpg|jpeg|png|gif|webp)",
                Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(answer);
        String photoUrl = null;
        if (matcher.find()) {
            photoUrl = matcher.group();
            System.out.println(photoUrl);
        } else {
            System.out.println("No photo URL found.");
        }

        if (photoUrl != null && !photoUrl.isBlank()) {
            try (InputStream inputStream = new URL(photoUrl).openStream()) {

                byte[] imageBytes = inputStream.readAllBytes();

                body.put("photoBase64", Base64.getEncoder().encodeToString(imageBytes));
                body.put("photoMimeType", detectMimeType(photoUrl));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ResponseEntity.ok(body);
    }
}
