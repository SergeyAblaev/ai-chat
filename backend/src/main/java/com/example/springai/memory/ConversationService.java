package com.example.springai.memory;

import com.example.api.ApiIds;
import com.example.api.AssistantMessage;
import com.example.multillm.AiProviderRouter;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationService {

    private final ChatService chatService;
    private final ChatMemory chatMemory;
    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();
    private final Map<String, List<ConversationMessage>> messages = new ConcurrentHashMap<>();

    public ConversationService(ChatService chatService, ChatMemory chatMemory) {
        this.chatService = chatService;
        this.chatMemory = chatMemory;
    }

    public Conversation create() {
        Conversation conversation = new Conversation(
                ApiIds.next("conv"),
                OffsetDateTime.now(),
                null
        );
        conversations.put(conversation.id(), conversation);
        messages.put(conversation.id(), new CopyOnWriteArrayList<>());
        return conversation;
    }

    public List<Conversation> findAll() {
        return conversations.values().stream()
                .sorted(Comparator.comparing(Conversation::createdAt).reversed())
                .toList();
    }

    public List<ConversationMessage> getMessages(String conversationId) {
        requireConversation(conversationId);
        return List.copyOf(messages.get(conversationId));
    }

    public void delete(String conversationId) {
        requireConversation(conversationId);
        chatMemory.clear(conversationId);
        conversations.remove(conversationId);
        messages.remove(conversationId);
    }

    public MessageResult addMessage(String conversationId, String prompt) {
        requireConversation(conversationId);
        if (prompt.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "prompt must not be blank");
        }
        AiProviderRouter.RoutingResult routing = chatService.chat(conversationId, prompt);
        OffsetDateTime userCreatedAt = OffsetDateTime.now();
        AssistantMessage assistant = AssistantMessage.create(routing.content());
        List<ConversationMessage> history = messages.get(conversationId);
        history.add(new ConversationMessage(
                ApiIds.next("msg"),
                "user",
                prompt,
                userCreatedAt
        ));
        history.add(new ConversationMessage(
                assistant.id(),
                assistant.role(),
                assistant.content(),
                assistant.createdAt()
        ));
        return new MessageResult(assistant, routing);
    }

    private void requireConversation(String conversationId) {
        if (!conversations.containsKey(conversationId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Conversation not found: " + conversationId
            );
        }
    }

    public record Conversation(
            String id,
            OffsetDateTime createdAt,
            String title
    ) {}

    public record ConversationMessage(
            String id,
            String role,
            String content,
            OffsetDateTime createdAt
    ) {}

    public record MessageResult(
            AssistantMessage message,
            AiProviderRouter.RoutingResult routing
    ) {}
}
