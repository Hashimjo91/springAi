package org.example.springaitest.services;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.example.springaitest.solrVectorStore.SolrAiSearchFilterExpressionConverter;
import org.example.springaitest.solrVectorStore.SolrSearchVectorStoreOptions;
import org.example.springaitest.solrVectorStore.SolrVectorStore;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {
    private ChatClient chatClient;
    private SolrVectorStore vectorStore;

    @Autowired
    public ChatService(OllamaChatModel chatModel, OllamaEmbeddingModel embeddingModel) {
        SolrClient solrClient = new HttpSolrClient.Builder("http://localhost:8983/solr/ms-marco").build();
        this.vectorStore = SolrVectorStore.builder(solrClient, embeddingModel)
                .options(new SolrSearchVectorStoreOptions())
                .filterExpressionConverter(new SolrAiSearchFilterExpressionConverter())
                .build();
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(getDefaultAdvisors())
                .build();
    }


    public ChatClient getChatClient() {
        return chatClient;
    }

    private List<Advisor> getDefaultAdvisors() {
        List<Advisor> advisors = new ArrayList<>();
        QuestionAnswerAdvisor questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder()
                .topK(1)
                .build());
        PromptChatMemoryAdvisor promptChatMemoryAdvisor = new PromptChatMemoryAdvisor(
                new InMemoryChatMemory()
        );
        SimpleLoggerAdvisor customLoggerAdvisor = new SimpleLoggerAdvisor();
        advisors.add(questionAnswerAdvisor);
        advisors.add(promptChatMemoryAdvisor);
        advisors.add(customLoggerAdvisor);
        return advisors;
    }
}