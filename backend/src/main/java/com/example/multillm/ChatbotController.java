package com.example.multillm;

import com.example.api.AssistantMessage;
import com.example.api.ExecutionDetails;
import com.example.api.ExecutionMode;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

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
        ExecutionDetails execution = ExecutionDetails.success(
                ExecutionMode.RESILIENT,
                result,
                aiModelCatalog.modelFor(result.provider())
        );
        return new ChatResponse(AssistantMessage.create(result.content()), execution);
    }

    record ChatRequest(@NotBlank String prompt) {}

    record ChatResponse(AssistantMessage message, ExecutionDetails execution) {}
}
