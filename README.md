# Doctor RAG Application — Spring AI + H2 + SimpleVectorStore

A complete Retrieval-Augmented Generation (RAG) application built with **Spring Boot 3.3**, **Spring AI 1.0 GA**, **H2** (relational store), and **SimpleVectorStore** (in-memory vector store). Uses Claude Sonnet as the LLM.

---

## Architecture

```
H2 Database (JPA)
     │
     ▼  (on startup, DataInitializer seeds 20 doctors)
DoctorsRepository
     │
     ▼  (RagService.indexDoctors → @PostConstruct)
SimpleVectorStore  ◄──── In-Memory Vector Index (cosine similarity)
     │
     ▼  (semantic search top-k=5)
Grounded Prompt Builder
     │
     ▼
Claude claude-3-5-sonnet-20241022
     │
     ▼
REST API  →  /api/rag/query
             /api/rag/reindex
             /api/dctors
             /api/doctors/stats
```

---

## Prerequisites

- Java 21+
- Maven 3.9+
- Anthropic API key

> **Important:** Spring AI's `SimpleVectorStore` requires an `EmbeddingModel` bean.
> This project uses Anthropic (chat only). For the embedding model, wire in a
> provider that supports embeddings — e.g. **Ollama `nomic-embed-text`** (free,
> local) or **OpenAI `text-embedding-ada-002`**. See *Embedding Model* section below.

---

## Quick Start

### 1. Clone and configure

```bash
git clone <repo>
cd doctor-rag
export ANTHROPIC_API_KEY=sk-ant-...
```

### 2. Add an Embedding Model (required)

#### Option A — Ollama (local, free)

```bash
# Install Ollama: https://ollama.ai
ollama pull nomic-embed-text
```

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
</dependency>
```

Add to `application.properties`:
```properties
spring.ai.ollama.embedding.model=nomic-embed-text
spring.ai.ollama.base-url=http://localhost:11434
```

#### Option B — OpenAI embeddings

```bash
export ANTHROPIC_API_KEY=sk-...
```

Add to `pom.xml`:
```xml
        <!-- Chat: Anthropic Claude -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-anthropic</artifactId>
</dependency>

        <!-- Embeddings: Ollama (auto-configures EmbeddingModel bean) -->
<dependency>
<groupId>org.springframework.ai</groupId>
<artifactId>spring-ai-starter-model-ollama</artifactId>
</dependency>

        <!-- SimpleVectorStore -->
<dependency>
<groupId>org.springframework.ai</groupId>
<artifactId>spring-ai-vector-store</artifactId>
</dependency>

```

Add to `application.properties`:
```properties
spring.ai.anthropic.api-key=${ANTHOPIC_API_KEY}
spring.ai.anthropic.chat.options.model=text-embedding-ada-002
spring.ai.anthropic.chat.options.max-tokens=1024
spring.ai.anthropic.chat.options.temperature=0.3

spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.embedding.model=nomic-embed-text
spring.ai.ollama.embedding.enabled=true
spring.ai.model.embedding=ollama

spring.ai.ollama.chat.enabled=false
```

### 3. Run

```bash
./mvnw spring-boot:run
```

The app:
1. Seeds 20 doctors into H2
2. Indexes all doctors as text documents into `SimpleVectorStore`
3. Starts on `http://localhost:8080/doctors-rag-ai-assitant.html`

---

## API Reference

### POST /api/rag/query
Ask a natural-language question about doctors.

```bash
curl -X POST http://localhost:8080/api/rag/query \
  -H "Content-Type: application/json" \
  -d '{"question": "Who are the good rating doctor in Cardiology department ?"}'
```

Response:
```json
{
  "answer": I appreciate your question, but I do not have rating information for any doctors in my available records. The doctor profiles I have access to do not include any rating scores or performance ratings.
  
  However, here are the Cardiology Department doctors I can share details about:

  | Detail | Dr. Robert Hayes | Dr. Anjali Mehta |
  |---|---|---|
  | Role | Chief of Cardiology | Senior Cardiologist |
  | Experience | 24 Years | 14 Years |
  | Location | New York, NY | New York, NY |
  | Email | robert.hayes@hospital.com | anjali.mehta@hospital.com |
  | Phone | +1-555-1002 | +1-555-1001 |
  
  ### 📌 Notable Achievements:
  - Dr. Hayes – Pioneer in cardiac electrophysiology, trained 50+ fellows, and received the AHA Lifetime Achievement Award.
  - Dr. Mehta – Performed 2,000+ catheterization procedures and leads the cardiac rehabilitation program.

  If you need ratings, please check with the hospital administration for official performance data. 😊
}
```

### POST /api/rag/reindex
Re-indexes all doctors into the vector store (useful after data changes).

```bash
curl -X POST http://localhost:8080/api/rag/reindex
```

### GET /api/doctors
Returns all 20 doctors as JSON.

### GET /api/doctors/stats
Returns total count and breakdown by department.

### GET /h2-console
H2 web console (JDBC URL: `jdbc:h2:mem:doctordb`).

---

## Project Structure

```
src/main/java/com/example/doctorrag/
├── DoctorRagApplication.java       # Entry point
├── config/
│   ├── AiConfig.java                 # VectorStore + ChatClient beans
│   └── DataInitializer.java          # Seeds 20 doctors into H2
├── controller/
│   ├── RagController.java            # POST /api/rag/query, /reindex
│   └── DoctorController.java       # GET /api/doctors, /stats
├── model/
│   └── Doctor.java                 # JPA entity + toDocument()
├── repository/
│   └── DoctorRepository.java       # Spring Data JPA
└── service/
    ├── RagService.java               # Core RAG pipeline
    └── DoctorService.java          # CRUD helpers
```

---

## RAG Pipeline Detail

```java
// 1. User asks a question
String question = "Who is the best doctor in Cardiology department?";

// 2. Semantic search on SimpleVectorStore (top-5 most relevant doctor docs)
List<Document> docs = vectorStore.similaritySearch(
    SearchRequest.builder().query(question).topK(5).build()
);

// 3. Build grounded prompt with retrieved context
String prompt = "CONTEXT:\n" + docs + "\n\nQUESTION:\n" + question;

// 4. Claude generates a grounded answer
String answer = chatClient.prompt().system(systemPrompt).user(prompt).call().content();
```

---

## Upgrading to Production

| Concern | Development | Production |
|---|---|---|
| Vector store | `SimpleVectorStore` (in-memory) | `PgVectorStore`, `ChromaVectorStore`, `WeaviateVectorStore` |
| Database | H2 in-memory | PostgreSQL, MySQL |
| Embeddings | Ollama local | OpenAI, Cohere, AWS Bedrock |
| LLM | Claude Sonnet | Claude Opus / Sonnet depending on latency SLAs |

---

## Example Questions

- "Who is the Dr. Priya Nair?"
- "Which doctors has point-of-care ultrasound (POCUS) skills?"
- "Provide All doctors skill report ?"
- "Tell me about the Pediatrics team."

