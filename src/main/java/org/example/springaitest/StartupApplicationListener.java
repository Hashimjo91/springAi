package org.example.springaitest;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StartupApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    ElasticsearchVectorStore vectorStore;



    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        List<Document> documentList = Helper.load();
        vectorStore.add(documentList);
    }
}
