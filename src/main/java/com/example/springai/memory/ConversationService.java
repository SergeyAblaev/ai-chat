package com.example.springai.memory;

import com.example.api.ApiIds;
import com.example.multillm.AiProviderRouter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationService {

    private final ChatService chatService;
    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();

    public ConversationService(ChatService chatService) {
        this.chatService = chatService;
    }

    public Conversation create() {
        Conversation conversation = new Conversation(
                ApiIds.next("conv"),
                OffsetDateTime.now(),
                null
        );
        conversations.put(conversation.id(), conversation);
        return conversation;
    }

    public AiProviderRouter.RoutingResult addMessage(String conversationId, String prompt) {
        if (!conversations.containsKey(conversationId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Conversation not found: " + conversationId
            );
        }
        if (prompt.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "prompt must not be blank");
        }
        return chatService.chat(conversationId, prompt);
    }

    public record Conversation(
            String id,
            OffsetDateTime createdAt,
            String title
    ) {}
}
