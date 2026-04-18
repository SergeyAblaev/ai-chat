package com.example.multillm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
class ChatbotService {

    private final ChatClient primaryChatClient;
    private final ChatClient secondaryChatClient;
    private final ChatClient geminiChatClient;
    private final AiProviderRouter aiProviderRouter;

    ChatbotService(
        ChatClient primaryChatClient,
        @Qualifier("secondaryChatClient") ChatClient secondaryChatClient,
        @Qualifier("geminiChatClient") ChatClient geminiChatClient,
        AiProviderRouter aiProviderRouter
    ) {
        this.primaryChatClient = primaryChatClient;
        this.secondaryChatClient = secondaryChatClient;
        this.geminiChatClient = geminiChatClient;
        this.aiProviderRouter = aiProviderRouter;
    }

    String chat(String prompt) {
        return aiProviderRouter.chat(provider -> clientFor(provider)
                .prompt(prompt)
                .call()
                .content());
    }

    private ChatClient clientFor(AiProvider provider) {
        return switch (provider) {
            case OPENAI -> primaryChatClient;
            case ANTHROPIC -> secondaryChatClient;
            case GEMINI -> geminiChatClient;
        };
    }

}
