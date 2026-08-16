package com.example.springai.memory;

import com.example.multillm.AiProvider;
import com.example.multillm.AiProviderRouter;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ChatService {

    private final Map<AiProvider, ChatClient> chatClients;
    private final AiProviderRouter aiProviderRouter;

    public ChatService(
            OpenAiChatModel openAiChatModel,
            AnthropicChatModel anthropicChatModel,
            GoogleGenAiChatModel googleGenAiChatModel,
            ChatMemory chatMemory,
            AiProviderRouter aiProviderRouter
    ) {
        this.chatClients = Map.of(
                AiProvider.OPENAI, memoryChatClient(openAiChatModel, chatMemory),
                AiProvider.ANTHROPIC, memoryChatClient(anthropicChatModel, chatMemory),
                AiProvider.GEMINI, memoryChatClient(googleGenAiChatModel, chatMemory)
        );
        this.aiProviderRouter = aiProviderRouter;
    }

    public AiProviderRouter.RoutingResult chat(String conversationId, String prompt) {
        return aiProviderRouter.chat(provider -> chatClients.get(provider)
                    .prompt()
                    .user(userMessage -> userMessage.text(prompt))
                    .advisors(advisor -> advisor.param(
                            ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .content());
    }

    private ChatClient memoryChatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

}
