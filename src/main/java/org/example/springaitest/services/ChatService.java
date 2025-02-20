package org.example.springaitest.services;

import org.example.springaitest.solrVectorStore.SolrVectorStore;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    public ChatService(OllamaChatModel chatModel, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
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