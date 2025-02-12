package org.example.springaitest;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.example.springaitest.solrVectorStore.SolrAiSearchFilterExpressionConverter;
import org.example.springaitest.solrVectorStore.SolrSearchVectorStoreOptions;
import org.example.springaitest.solrVectorStore.SolrVectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StartupApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    OllamaEmbeddingModel embeddingModel;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        SolrClient solrClient = new HttpSolrClient.Builder("http://localhost:8983/solr/ms-marco").build();
        SolrVectorStore vectorStore = SolrVectorStore.builder(solrClient, embeddingModel)
                .options(new SolrSearchVectorStoreOptions())
                .filterExpressionConverter(new SolrAiSearchFilterExpressionConverter())
                .build();
        List<Document> documentList = Helper.load();
        vectorStore.add(documentList);
    }
}
