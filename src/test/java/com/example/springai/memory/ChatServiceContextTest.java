package com.example.springai.memory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.ai.openai.api-key=test",
        "spring.ai.openai.chat.options.model=test",
        "spring.ai.anthropic.api-key=test",
        "spring.ai.anthropic.chat.options.model=test",
        "spring.ai.google.genai.api-key=test",
        "spring.ai.google.genai.chat.options.model=test"
})
class ChatServiceContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ChatMemory chatMemory;

    @AfterEach
    void resetRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void createsSessionScopedChatServiceWithAllConfiguredProviders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ChatService chatService = applicationContext.getBean(ChatService.class);

        assertThat(chatService).isNotNull();
        assertThat(chatService.getConversationId()).isNotBlank();
        assertThat(chatMemory.get(chatService.getConversationId())).isEmpty();
    }
}
