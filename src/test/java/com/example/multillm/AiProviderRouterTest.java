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

        String firstResponse = router.chat(provider -> {
            calls.add(provider);
            if (provider == AiProvider.OPENAI) {
                throw new RuntimeException("OpenAI is unavailable");
            }
            return "Anthropic response";
        });

        assertThat(firstResponse).isEqualTo("Anthropic response");
        assertThat(calls).containsExactly(
                AiProvider.OPENAI,
                AiProvider.OPENAI,
                AiProvider.OPENAI,
                AiProvider.ANTHROPIC
        );
        assertThat(router.getSuccessfulProvider()).isEqualTo(AiProvider.ANTHROPIC);

        calls.clear();

        String secondResponse = router.chat(provider -> {
            calls.add(provider);
            return "Remembered provider response";
        });

        assertThat(secondResponse).isEqualTo("Remembered provider response");
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
        String response = router.chat(provider -> {
            calls.add(provider);
            if (provider == AiProvider.GEMINI) {
                throw new RuntimeException("Gemini is unavailable");
            }
            return "OpenAI response";
        });

        assertThat(response).isEqualTo("OpenAI response");
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
