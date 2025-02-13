package org.example.springaitest;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import jakarta.servlet.http.HttpServletRequest;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Helper {
    public static List<Document> load() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] docs;
        List<Document> documents = new LinkedList<>();
        try {

            docs = resolver.getResources("docs/*.json");
            Gson gson = new Gson();
            for (Resource cert : docs) {
                JsonReader reader = new JsonReader(new FileReader(cert.getFile()));
                Map<String, String> metaDataMap = gson.fromJson(reader, HashMap.class);
                String id = metaDataMap.get("doi");
                String text = metaDataMap.get("content");
                Document doc = new Document(id, text, new HashMap<>(metaDataMap));
                documents.add(doc);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return documents;
    }

    public static User getUserId(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null || userId.isBlank()) {
            userId = "10001";
        }
        return new User(Long.parseLong(userId), "Hashem");
    }

    public record User(long userId, String name){

    }
}
