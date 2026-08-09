package com.example.multillm;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiProviderRouterTest {

    @Test
    void remembersSuccessfulProviderAndUsesItFirstForNextRequest() {
        AiProviderRouter router = new AiProviderRouter();
        List<AiProvider> calls = new ArrayList<>();

        AiProviderRouter.RoutingResult firstResult = router.chat(provider -> {
            calls.add(provider);
            if (provider == AiProvider.OPENAI) {
                throw new RuntimeException("OpenAI is unavailable");
            }
            return "Anthropic response";
        });

        assertThat(firstResult.content()).isEqualTo("Anthropic response");
        assertThat(firstResult.provider()).isEqualTo(AiProvider.ANTHROPIC);
        assertThat(firstResult.attemptCount()).isEqualTo(4);
        assertThat(firstResult.fallbackUsed()).isTrue();
        assertThat(firstResult.attempts())
                .extracting(AiProviderRouter.Attempt::status)
                .containsExactly(
                        AiProviderRouter.AttemptStatus.FAILED,
                        AiProviderRouter.AttemptStatus.FAILED,
                        AiProviderRouter.AttemptStatus.FAILED,
                        AiProviderRouter.AttemptStatus.SUCCESS
                );
        assertThat(calls).containsExactly(
                AiProvider.OPENAI,
                AiProvider.OPENAI,
                AiProvider.OPENAI,
                AiProvider.ANTHROPIC
        );
        assertThat(router.getSuccessfulProvider()).isEqualTo(AiProvider.ANTHROPIC);

        calls.clear();

        AiProviderRouter.RoutingResult secondResult = router.chat(provider -> {
            calls.add(provider);
            return "Remembered provider response";
        });

        assertThat(secondResult.content()).isEqualTo("Remembered provider response");
        assertThat(secondResult.provider()).isEqualTo(AiProvider.ANTHROPIC);
        assertThat(secondResult.attemptCount()).isEqualTo(1);
        assertThat(secondResult.fallbackUsed()).isFalse();
        assertThat(calls).containsExactly(AiProvider.ANTHROPIC);
    }

    @Test
    void switchesRememberedProviderWhenItFailsAndFallbackSucceeds() {
        AiProviderRouter router = new AiProviderRouter();

        router.chat(provider -> {
            if (provider != AiProvider.GEMINI) {
                throw new RuntimeException(provider + " is unavailable");
            }
            return "Gemini response";
        });

        List<AiProvider> calls = new ArrayList<>();
        AiProviderRouter.RoutingResult result = router.chat(provider -> {
            calls.add(provider);
            if (provider == AiProvider.GEMINI) {
                throw new RuntimeException("Gemini is unavailable");
            }
            return "OpenAI response";
        });

        assertThat(result.content()).isEqualTo("OpenAI response");
        assertThat(result.provider()).isEqualTo(AiProvider.OPENAI);
        assertThat(calls).containsExactly(
                AiProvider.GEMINI,
                AiProvider.GEMINI,
                AiProvider.GEMINI,
                AiProvider.OPENAI
        );
        assertThat(router.getSuccessfulProvider()).isEqualTo(AiProvider.OPENAI);
    }

    @Test
    void doesNotTreatDatabaseFailureAsProviderFailure() {
        AiProviderRouter router = new AiProviderRouter();
        List<AiProvider> calls = new ArrayList<>();

        assertThatThrownBy(() -> router.chat(provider -> {
            calls.add(provider);
            throw new DataRetrievalFailureException("Chat memory is unavailable");
        })).isInstanceOf(DataAccessException.class);

        assertThat(calls).containsExactly(AiProvider.OPENAI);
        assertThat(router.getSuccessfulProvider()).isNull();
    }
}
