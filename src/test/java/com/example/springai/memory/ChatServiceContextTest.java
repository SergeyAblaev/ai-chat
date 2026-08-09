package com.example.springai.memory;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    private ConversationService conversationService;

    @Autowired
    private ChatMemory chatMemory;

    @Test
    void createsExplicitConversationForChatMemory() {
        ConversationService.Conversation conversation = conversationService.create();

        assertThat(conversation.id()).startsWith("conv_");
        assertThat(conversation.createdAt()).isNotNull();
        assertThat(conversation.title()).isNull();
        assertThat(chatMemory.get(conversation.id())).isEmpty();
    }
}
