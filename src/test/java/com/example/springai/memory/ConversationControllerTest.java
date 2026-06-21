package com.example.springai.memory;

import com.example.api.ExecutionMode;
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
import static org.mockito.Mockito.when;
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
        when(service.addMessage("conv_01J", "Explain Spring AI")).thenReturn(routingResult);
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
        assertThat(response.execution().durationMs()).isEqualTo(735);
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
        when(service.addMessage("conv_01J", "Explain Spring AI")).thenReturn(routingResult);
        when(modelCatalog.modelFor(AiProvider.OPENAI)).thenReturn("gpt-5-mini");
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/conversations/conv_01J/messages")
                        .contentType("application/json")
                        .content("{\"prompt\":\"Explain Spring AI\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message.content").value("Spring AI explanation"))
                .andExpect(jsonPath("$.execution.mode").value("CONTEXT"));
    }
}
