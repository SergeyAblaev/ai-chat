package com.example.springai.memory;

import com.example.api.AssistantMessage;
import com.example.api.AttemptExecution;
import com.example.api.ExecutionMode;
import com.example.api.ExecutionStatus;
import com.example.multillm.AiModelCatalog;
import com.example.multillm.AiProvider;
import com.example.multillm.AiProviderRouter;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConversationControllerTest {

    @Test
    void createsConversationWithCreatedStatus() {
        ConversationService service = mock(ConversationService.class);
        AiModelCatalog modelCatalog = mock(AiModelCatalog.class);
        ConversationController controller = new ConversationController(service, modelCatalog);
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-08-07T16:02:31.425+07:00");
        when(service.create()).thenReturn(new ConversationService.Conversation("conv_01J", createdAt, null));

        ResponseEntity<ConversationController.ConversationResponse> response = controller.createConversation();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo("conv_01J");
        assertThat(response.getBody().createdAt()).isEqualTo(createdAt);
        assertThat(response.getBody().title()).isNull();
    }

    @Test
    void returnsContextMessageExecution() {
        ConversationService service = mock(ConversationService.class);
        AiModelCatalog modelCatalog = mock(AiModelCatalog.class);
        ConversationController controller = new ConversationController(service, modelCatalog);
        AiProviderRouter.RoutingResult routingResult = new AiProviderRouter.RoutingResult(
                "Spring AI explanation",
                AiProvider.OPENAI,
                735,
                List.of(new AiProviderRouter.Attempt(
                        AiProvider.OPENAI,
                        1,
                        AiProviderRouter.AttemptStatus.SUCCESS,
                        735
                ))
        );
        AssistantMessage message = new AssistantMessage(
                "msg_01J",
                "assistant",
                "Spring AI explanation",
                OffsetDateTime.parse("2026-08-07T16:03:12.150+07:00")
        );
        when(service.addMessage("conv_01J", "Explain Spring AI"))
                .thenReturn(new ConversationService.MessageResult(message, routingResult));
        when(modelCatalog.modelFor(AiProvider.OPENAI)).thenReturn("gpt-5-mini");

        ConversationController.ContextChatResponse response = controller.addMessage(
                "conv_01J",
                new ChatRequest("Explain Spring AI")
        );

        assertThat(response.message().id()).startsWith("msg_");
        assertThat(response.message().content()).isEqualTo("Spring AI explanation");
        assertThat(response.execution().requestId()).startsWith("req_");
        assertThat(response.execution().mode()).isEqualTo(ExecutionMode.CONTEXT);
        assertThat(response.execution().provider()).isEqualTo(AiProvider.OPENAI);
        assertThat(response.execution().model()).isEqualTo("gpt-5-mini");
        assertThat(response.execution().status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.execution().fallbackUsed()).isFalse();
        assertThat(response.execution().attemptCount()).isEqualTo(1);
        assertThat(response.execution().durationMs()).isEqualTo(735);
        assertThat(response.execution().attempts())
                .extracting(AttemptExecution::status)
                .containsExactly(
                        AiProviderRouter.AttemptStatus.SUCCESS,
                        AiProviderRouter.AttemptStatus.SKIPPED,
                        AiProviderRouter.AttemptStatus.SKIPPED
                );
    }

    @Test
    void bindsConversationIdFromTheRequestPath() throws Exception {
        ConversationService service = mock(ConversationService.class);
        AiModelCatalog modelCatalog = mock(AiModelCatalog.class);
        ConversationController controller = new ConversationController(service, modelCatalog);
        AiProviderRouter.RoutingResult routingResult = new AiProviderRouter.RoutingResult(
                "Spring AI explanation",
                AiProvider.OPENAI,
                735,
                List.of(new AiProviderRouter.Attempt(
                        AiProvider.OPENAI,
                        1,
                        AiProviderRouter.AttemptStatus.SUCCESS,
                        735
                ))
        );
        AssistantMessage message = new AssistantMessage(
                "msg_01J",
                "assistant",
                "Spring AI explanation",
                OffsetDateTime.parse("2026-08-07T16:03:12.150+07:00")
        );
        when(service.addMessage("conv_01J", "Explain Spring AI"))
                .thenReturn(new ConversationService.MessageResult(message, routingResult));
        when(modelCatalog.modelFor(AiProvider.OPENAI)).thenReturn("gpt-5-mini");
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/conversations/conv_01J/messages")
                        .contentType("application/json")
                        .content("{\"prompt\":\"Explain Spring AI\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message.content").value("Spring AI explanation"))
                .andExpect(jsonPath("$.execution.mode").value("CONTEXT"))
                .andExpect(jsonPath("$.execution.status").value("SUCCESS"))
                .andExpect(jsonPath("$.execution.attempts[1].status").value("SKIPPED"));
    }

    @Test
    void exposesConversationListHistoryAndDeleteEndpoints() throws Exception {
        ConversationService service = mock(ConversationService.class);
        AiModelCatalog modelCatalog = mock(AiModelCatalog.class);
        ConversationController controller = new ConversationController(service, modelCatalog);
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-08-07T16:02:31.425+07:00");
        when(service.findAll()).thenReturn(List.of(
                new ConversationService.Conversation("conv_01J", createdAt, "Spring AI")
        ));
        when(service.getMessages("conv_01J")).thenReturn(List.of(
                new ConversationService.ConversationMessage(
                        "msg_user",
                        "user",
                        "Explain Spring AI",
                        createdAt
                )
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("conv_01J"))
                .andExpect(jsonPath("$[0].title").value("Spring AI"));
        mockMvc.perform(get("/api/conversations/conv_01J/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("user"))
                .andExpect(jsonPath("$[0].content").value("Explain Spring AI"));
        mockMvc.perform(delete("/api/conversations/conv_01J"))
                .andExpect(status().isNoContent());

        verify(service).delete("conv_01J");
    }
}
