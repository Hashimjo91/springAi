package org.example.springaitest;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.example.springaitest.solrVectorStore.SolrAiSearchFilterExpressionConverter;
import org.example.springaitest.solrVectorStore.SolrSearchVectorStoreOptions;
import org.example.springaitest.solrVectorStore.SolrVectorStore;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.AssistantPromptTemplate;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
class AIController {
    final VectorStore vectorStore;
    private final OllamaChatModel chatModel;
    @Value("classpath:/prompt/system-prompt.st")
    private Resource systemPrompt;
    @Value("classpath:/prompt/assistant-prompt.st")
    private Resource assistantPrompt;
    @Autowired
    public AIController(OllamaChatModel chatModel, OllamaEmbeddingModel embeddingModel) {
        this.chatModel = chatModel;
        SolrClient solrClient = new HttpSolrClient.Builder("http://localhost:8983/solr/ms-marco").build();
        this.vectorStore = SolrVectorStore.builder(solrClient, embeddingModel)
                .options(new SolrSearchVectorStoreOptions())
                .filterExpressionConverter(new SolrAiSearchFilterExpressionConverter())
                .build();
    }

    @GetMapping("/ai")
    Map<String, String> completion(
            HttpServletRequest request,
            @RequestParam(value = "message") String message,
            @RequestParam(value = "doi") String doi
    ) {

        Message systemMessage = getSystemMessage();
        UserMessage userMessage = new UserMessage(message);
        long userId = Helper.getUserId(request);
        AssistantPromptTemplate assistantPromptTemplate = new AssistantPromptTemplate(assistantPrompt);
        Message assistantPromptTemplateMessage = assistantPromptTemplate.createMessage(Map.of("input", message));
        Prompt prompt = new Prompt(List.of(assistantPromptTemplateMessage));
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(getDefaultAdvisors())
                .build();
        return Map.of(
                "aiResponse", chatClient
                        .prompt(prompt)
                        .advisors(a -> a
                                .param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "doi == '"+doi+"'")
                                .param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, userId)
                                .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
                        )
                        .user(message)
                        .call()
                        .content()
        );
    }

    private List<Advisor> getDefaultAdvisors() {
        List<Advisor> advisors = new ArrayList<>();
        QuestionAnswerAdvisor questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder()
                .topK(1)
                .build());
        PromptChatMemoryAdvisor promptChatMemoryAdvisor = new PromptChatMemoryAdvisor(
            new InMemoryChatMemory()
        );
        advisors.add(questionAnswerAdvisor);
        advisors.add(promptChatMemoryAdvisor);
        return advisors;
    }

    private Message getSystemMessage() {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPrompt);
        return systemPromptTemplate.createMessage();
    }
}
