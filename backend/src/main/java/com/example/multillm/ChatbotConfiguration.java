package com.example.multillm;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
class ChatbotConfiguration {

    @Bean
    @Primary
    ChatClient primaryChatClient(OpenAiChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    @Bean
    ChatClient secondaryChatClient(AnthropicChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    @Bean
    ChatClient geminiChatClient(GoogleGenAiChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

}
