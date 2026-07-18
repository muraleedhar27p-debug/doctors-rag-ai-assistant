package com.spring.ai.rag.config;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    // Spring auto-configures EmbeddingModel from spring-ai-starter-model-ollama
    // + application.properties. Just inject it directly — no manual OllamaApi needed.

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    @Primary
    @Qualifier("chatModel")
    public ChatClient chatClient(AnthropicChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}