package org.example.springaitest;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.example.springaitest.solrVectorStore.SolrAiSearchFilterExpressionConverter;
import org.example.springaitest.solrVectorStore.SolrVectorStore;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
class Config {
    private final Environment env;
    public Config(Environment env) {
        this.env = env;
    }

    @Bean
    SolrClient solrClient() {
        return new HttpSolrClient.Builder(env.getProperty("solr.url"))
                .build();
    }
    @Bean
    VectorStore vectorStore(SolrClient client, EmbeddingModel model) {
        return SolrVectorStore.builder(client, model)
                .collection(env.getProperty("solr.collection"))
                .build();
    }

}
