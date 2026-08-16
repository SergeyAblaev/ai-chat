package com.example.multillm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AiModelCatalog {

    private final String openAiModel;
    private final String anthropicModel;
    private final String geminiModel;

    public AiModelCatalog(
            @Value("${spring.ai.openai.chat.options.model}") String openAiModel,
            @Value("${spring.ai.anthropic.chat.options.model}") String anthropicModel,
            @Value("${spring.ai.google.genai.chat.options.model}") String geminiModel
    ) {
        this.openAiModel = openAiModel;
        this.anthropicModel = anthropicModel;
        this.geminiModel = geminiModel;
    }

    public String modelFor(AiProvider provider) {
        return switch (provider) {
            case OPENAI -> openAiModel;
            case ANTHROPIC -> anthropicModel;
            case GEMINI -> geminiModel;
        };
    }
}
