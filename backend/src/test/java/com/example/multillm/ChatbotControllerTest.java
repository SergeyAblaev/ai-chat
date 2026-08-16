package com.example.multillm;

import com.example.api.AttemptExecution;
import com.example.api.ExecutionMode;
import com.example.api.ExecutionStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatbotControllerTest {

    @Test
    void returnsMessageAndSuccessfulFallbackExecution() {
        ChatbotService chatbotService = mock(ChatbotService.class);
        AiModelCatalog modelCatalog = mock(AiModelCatalog.class);
        ChatbotController controller = new ChatbotController(chatbotService, modelCatalog);
        AiProviderRouter.RoutingResult routingResult = new AiProviderRouter.RoutingResult(
                "Response from Anthropic",
                AiProvider.ANTHROPIC,
                4_231,
                List.of(
                        new AiProviderRouter.Attempt(AiProvider.OPENAI, 1, AiProviderRouter.AttemptStatus.FAILED, 812),
                        new AiProviderRouter.Attempt(AiProvider.OPENAI, 2, AiProviderRouter.AttemptStatus.FAILED, 906),
                        new AiProviderRouter.Attempt(AiProvider.OPENAI, 3, AiProviderRouter.AttemptStatus.FAILED, 794),
                        new AiProviderRouter.Attempt(AiProvider.ANTHROPIC, 1, AiProviderRouter.AttemptStatus.SUCCESS, 1_419)
                )
        );
        when(chatbotService.chat("hello")).thenReturn(routingResult);
        when(modelCatalog.modelFor(AiProvider.ANTHROPIC)).thenReturn("claude-sonnet");

        ChatbotController.ChatResponse response = controller.chat(new ChatbotController.ChatRequest("hello"));

        assertThat(response.message().id()).startsWith("msg_");
        assertThat(response.message().role()).isEqualTo("assistant");
        assertThat(response.message().content()).isEqualTo("Response from Anthropic");
        assertThat(response.message().createdAt()).isNotNull();
        assertThat(response.execution().requestId()).startsWith("req_");
        assertThat(response.execution().mode()).isEqualTo(ExecutionMode.RESILIENT);
        assertThat(response.execution().provider()).isEqualTo(AiProvider.ANTHROPIC);
        assertThat(response.execution().model()).isEqualTo("claude-sonnet");
        assertThat(response.execution().status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.execution().fallbackUsed()).isTrue();
        assertThat(response.execution().attemptCount()).isEqualTo(4);
        assertThat(response.execution().durationMs()).isEqualTo(4_231);
        assertThat(response.execution().attempts())
                .extracting(AttemptExecution::status)
                .containsExactly(
                        AiProviderRouter.AttemptStatus.FAILED,
                        AiProviderRouter.AttemptStatus.FAILED,
                        AiProviderRouter.AttemptStatus.FAILED,
                        AiProviderRouter.AttemptStatus.SUCCESS,
                        AiProviderRouter.AttemptStatus.SKIPPED
                );
    }
}
