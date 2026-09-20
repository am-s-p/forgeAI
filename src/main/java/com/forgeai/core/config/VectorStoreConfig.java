package com.forgeai.core.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class VectorStoreConfig {

    @Value("${spring.ai.vectorstore.simple.path:./data/vector_store.json}")
    private String vectorStorePath;

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        File file = new File(vectorStorePath);
        if (file.exists() && file.isFile()) {
            vectorStore.load(file);
        }
        return vectorStore;
    }
}
