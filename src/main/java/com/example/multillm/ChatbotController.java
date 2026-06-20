package com.example.multillm;

import com.example.api.ApiIds;
import com.example.api.AssistantMessage;
import com.example.api.ExecutionMode;
import com.example.api.ExecutionStatus;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@RestController
class ChatbotController {

    private final ChatbotService chatbotService;
    private final AiModelCatalog aiModelCatalog;

    ChatbotController(ChatbotService chatbotService, AiModelCatalog aiModelCatalog) {
        this.chatbotService = chatbotService;
        this.aiModelCatalog = aiModelCatalog;
    }

    @Operation(
            summary = "Resilient stateless request",
            description = "Fault-tolerant request with retry and provider fallback execution details"
    )
    @PostMapping("/api/chatbot/chat")
    ChatResponse chat(@RequestBody @Valid ChatRequest request) {
        AiProviderRouter.RoutingResult result = chatbotService.chat(request.prompt);
        List<AttemptExecution> attempts = result.attempts().stream()
                .map(attempt -> new AttemptExecution(
                        attempt.provider(),
                        attempt.attempt(),
                        attempt.status(),
                        attempt.durationMs()
                ))
                .toList();
        ResilientExecution execution = new ResilientExecution(
                ApiIds.next("req"),
                ExecutionMode.RESILIENT,
                result.provider(),
                aiModelCatalog.modelFor(result.provider()),
                ExecutionStatus.SUCCESS,
                result.fallbackUsed(),
                result.attemptCount(),
                result.durationMs(),
                attempts
        );
        return new ChatResponse(AssistantMessage.create(result.content()), execution);
    }

    record ChatRequest(@NotBlank String prompt) {}

    record ChatResponse(AssistantMessage message, ResilientExecution execution) {}

    record ResilientExecution(
            String requestId,
            ExecutionMode mode,
            AiProvider provider,
            String model,
            ExecutionStatus status,
            boolean fallbackUsed,
            int attemptCount,
            long durationMs,
            List<AttemptExecution> attempts
    ) {}

    record AttemptExecution(
            AiProvider provider,
            int attempt,
            AiProviderRouter.AttemptStatus status,
            long durationMs
    ) {}
}
