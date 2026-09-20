package com.forgeai.core.knowledge;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class DocumentService {

    private final SimpleVectorStore vectorStore;

    @Value("${spring.ai.vectorstore.simple.path:./data/vector_store.json}")
    private String vectorStorePath;

    public DocumentService(SimpleVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public int ingestFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) filename = "unknown";
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        Resource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        List<Document> documents;
        if ("pdf".equals(extension) || "docx".equals(extension)) {
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            documents = reader.get();
        } else {
            TextReader reader = new TextReader(resource);
            reader.getCustomMetadata().put("filename", filename);
            documents = reader.get();
        }

        TokenTextSplitter splitter = TokenTextSplitter.builder().build();
        List<Document> chunks = splitter.apply(documents);
        
        vectorStore.add(chunks);
        vectorStore.save(new File(vectorStorePath));

        return chunks.size();
    }
}
