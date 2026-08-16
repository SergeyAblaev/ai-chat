package com.example.springai.memory;

import com.example.multillm.AiProvider;
import com.example.multillm.AiProviderRouter;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationServiceTest {

    @Test
    void routesMessageWithExplicitConversationId() {
        ChatService chatService = mock(ChatService.class);
        ConversationService service = new ConversationService(chatService, mock(ChatMemory.class));
        ConversationService.Conversation conversation = service.create();
        AiProviderRouter.RoutingResult expected = new AiProviderRouter.RoutingResult(
                "answer",
                AiProvider.OPENAI,
                735,
                List.of(new AiProviderRouter.Attempt(
                        AiProvider.OPENAI,
                        1,
                        AiProviderRouter.AttemptStatus.SUCCESS,
                        735
                ))
        );
        when(chatService.chat(conversation.id(), "Explain Spring AI")).thenReturn(expected);

        ConversationService.MessageResult actual = service.addMessage(
                conversation.id(),
                "Explain Spring AI"
        );

        assertThat(actual.routing()).isSameAs(expected);
        assertThat(actual.message().content()).isEqualTo("answer");
        assertThat(service.getMessages(conversation.id()))
                .extracting(ConversationService.ConversationMessage::role)
                .containsExactly("user", "assistant");
        assertThat(service.getMessages(conversation.id()).get(1).id())
                .isEqualTo(actual.message().id());
        verify(chatService).chat(conversation.id(), "Explain Spring AI");
    }

    @Test
    void listsAndDeletesConversationWithItsMemory() {
        ChatMemory chatMemory = mock(ChatMemory.class);
        ConversationService service = new ConversationService(mock(ChatService.class), chatMemory);
        ConversationService.Conversation conversation = service.create();

        assertThat(service.findAll()).containsExactly(conversation);

        service.delete(conversation.id());

        assertThat(service.findAll()).isEmpty();
        verify(chatMemory).clear(conversation.id());
        assertThatThrownBy(() -> service.getMessages(conversation.id()))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void rejectsUnknownConversation() {
        ConversationService service = new ConversationService(
                mock(ChatService.class),
                mock(ChatMemory.class)
        );

        assertThatThrownBy(() -> service.addMessage("conv_missing", "hello"))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
