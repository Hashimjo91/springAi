package org.example.springaitest;

import org.example.springaitest.services.ChatService;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.AssistantPromptTemplate;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
class AIController {

    @Value("classpath:/prompt/system-prompt.st")
    private Resource systemPrompt;
    @Value("classpath:/prompt/assistant-prompt.st")
    private Resource assistantPrompt;

    private final ChatService chatService;


    public AIController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/ai")
    Map<String, String> completion(
            HttpServletRequest request,
            @RequestParam(value = "message") String message,
            @RequestParam(value = "doi") String doi
    ) {
        Helper.User user = Helper.getUserId(request);

        Message systemMessage = getSystemMessage();
        Message assistantPromptTemplateMessage = getAssistantPromptTemplateMessage(message, user.name());

        return Map.of(
                "aiResponse", chatService.getChatClient()
                        .prompt(new Prompt(List.of(
                                systemMessage,
                                assistantPromptTemplateMessage
                        )))
                        .advisors(a -> a
                                .param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "doi == '"+doi+"'")
                                .param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, user.userId())
                                .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
                        )
                        .user(message)
                        .call()
                        .content()
        );
    }

    private Message getAssistantPromptTemplateMessage(String message, String userName) {
        AssistantPromptTemplate assistantPromptTemplate = new AssistantPromptTemplate(assistantPrompt);
        return assistantPromptTemplate.createMessage(Map.of("input", message, "user", userName));
    }


    private Message getSystemMessage() {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPrompt);
        return systemPromptTemplate.createMessage();
    }
}
