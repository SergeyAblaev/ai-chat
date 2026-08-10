package com.example.springai.memory;

import com.example.api.AssistantMessage;
import com.example.api.ExecutionDetails;
import com.example.api.ExecutionMode;
import com.example.multillm.AiModelCatalog;
import com.example.multillm.AiProviderRouter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;

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
        ConversationResponse response = toResponse(conversation);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "List context conversations")
    @GetMapping
    public List<ConversationResponse> listConversations() {
        return conversationService.findAll().stream()
                .map(ConversationController::toResponse)
                .toList();
    }

    @Operation(summary = "List messages in a context conversation")
    @GetMapping("/{conversationId}/messages")
    public List<ConversationMessageResponse> listMessages(@PathVariable String conversationId) {
        return conversationService.getMessages(conversationId).stream()
                .map(message -> new ConversationMessageResponse(
                        message.id(),
                        message.role(),
                        message.content(),
                        message.createdAt()
                ))
                .toList();
    }

    @Operation(summary = "Delete a context conversation and its message history")
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable String conversationId) {
        conversationService.delete(conversationId);
        return ResponseEntity.noContent().build();
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
        ConversationService.MessageResult result = conversationService.addMessage(
                conversationId,
                request.prompt()
        );
        AiProviderRouter.RoutingResult routing = result.routing();
        ExecutionDetails execution = ExecutionDetails.success(
                ExecutionMode.CONTEXT,
                routing,
                aiModelCatalog.modelFor(routing.provider())
        );
        return new ContextChatResponse(result.message(), execution);
    }

    private static ConversationResponse toResponse(ConversationService.Conversation conversation) {
        return new ConversationResponse(
                conversation.id(),
                conversation.createdAt(),
                conversation.title()
        );
    }

    public record ConversationResponse(
            String id,
            OffsetDateTime createdAt,
            String title
    ) {}

    public record ContextChatResponse(
            AssistantMessage message,
            ExecutionDetails execution
    ) {}

    public record ConversationMessageResponse(
            String id,
            String role,
            String content,
            OffsetDateTime createdAt
    ) {}
}
