package com.example.multillm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;

@Service
class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    private final ChatClient primaryChatClient;
    private final ChatClient secondaryChatClient;

    ChatbotService(
        ChatClient primaryChatClient,
        @Qualifier("secondaryChatClient") ChatClient secondaryChatClient
    ) {
        this.primaryChatClient = primaryChatClient;
        this.secondaryChatClient = secondaryChatClient;
    }

    @Retryable(retryFor = Exception.class, maxAttempts = 3)
    String chat(String prompt) {
        logger.debug("Process prompt '{}' with primary LLM. Attempt #{}",
            prompt, RetrySynchronizationManager.getContext().getRetryCount() + 1);
        return primaryChatClient
            .prompt(prompt)
            .call()
            .content();
    }

    @Recover
    String chat(Exception exception, String prompt) {
        logger.warn("Primary LLM failure. Error received: {}", exception.getMessage());
        logger.debug("Process prompt '{}' with secondary LLM", prompt);
        try {
            return secondaryChatClient
                .prompt(prompt)
                .call()
                .content();
        } catch (Exception e) {
            logger.warn("Secondary LLM failure: {}", e.getMessage());
            throw new RuntimeException("Failed to process prompt with primary and secondary LLMs", e);
        }
    }

}