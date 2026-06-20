package com.example.springai.memory;

import com.example.api.ApiIds;
import com.example.api.AssistantMessage;
import com.example.api.ExecutionMode;
import com.example.multillm.AiModelCatalog;
import com.example.multillm.AiProvider;
import com.example.multillm.AiProviderRouter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final AiModelCatalog aiModelCatalog;

    public ConversationController(
            ConversationService conversationService,
            AiModelCatalog aiModelCatalog
    ) {
        this.conversationService = conversationService;
        this.aiModelCatalog = aiModelCatalog;
    }

    @Operation(summary = "Create a context conversation")
    @PostMapping
    public ResponseEntity<ConversationResponse> createConversation() {
        ConversationService.Conversation conversation = conversationService.create();
        ConversationResponse response = new ConversationResponse(
                conversation.id(),
                conversation.createdAt(),
                conversation.title()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Send a message in a context conversation",
            description = "Uses the explicit conversation ID as the Spring AI chat-memory key"
    )
    @PostMapping("/{conversationId}/messages")
    public ContextChatResponse addMessage(
            @PathVariable String conversationId,
            @RequestBody @Valid ChatRequest request
    ) {
        AiProviderRouter.RoutingResult result = conversationService.addMessage(
                conversationId,
                request.prompt()
        );
        ContextExecution execution = new ContextExecution(
                ApiIds.next("req"),
                ExecutionMode.CONTEXT,
                result.provider(),
                aiModelCatalog.modelFor(result.provider()),
                result.durationMs()
        );
        return new ContextChatResponse(AssistantMessage.create(result.content()), execution);
    }

    public record ConversationResponse(
            String id,
            OffsetDateTime createdAt,
            String title
    ) {}

    public record ContextChatResponse(
            AssistantMessage message,
            ContextExecution execution
    ) {}

    public record ContextExecution(
            String requestId,
            ExecutionMode mode,
            AiProvider provider,
            String model,
            long durationMs
    ) {}
}
