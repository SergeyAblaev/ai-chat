package com.example.multillm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class AiProviderRouter {

    private static final int PREFERRED_PROVIDER_MAX_ATTEMPTS = 3;

    private final AtomicReference<AiProvider> successfulProvider = new AtomicReference<>();

    public String chat(ProviderCall providerCall) {
        AiProvider preferredProvider = successfulProvider.get();
        AiProvider retryProvider = preferredProvider == null
                ? AiProvider.OPENAI
                : preferredProvider;
        Exception lastException = null;

        for (AiProvider provider : providersStartingWith(preferredProvider)) {
            int maxAttempts = provider == retryProvider
                    ? PREFERRED_PROVIDER_MAX_ATTEMPTS
                    : 1;

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    log.debug("Attempting to process prompt with {}. Attempt #{}",
                            provider, attempt);
                    String response = providerCall.call(provider);
                    successfulProvider.set(provider);
                    log.info("Setted successfulProvider is {}", provider);
                    return response;
                } catch (DataAccessException exception) {
                    throw exception;
                } catch (Exception exception) {
                    lastException = exception;
                    log.warn("{} failure on attempt #{}: {}",
                            provider, attempt, exception.getMessage());
                }
            }
        }

        throw new RuntimeException("Failed to process prompt with all configured LLMs",
                lastException);
    }

    public AiProvider getSuccessfulProvider() {
        return successfulProvider.get();
    }

    private List<AiProvider> providersStartingWith(AiProvider preferredProvider) {
        if (preferredProvider == null) {
            return new ArrayList<>(Arrays.asList(AiProvider.values()));
        }

        List<AiProvider> providers = new ArrayList<>();
        providers.add(preferredProvider);
        Arrays.stream(AiProvider.values())
                .filter(provider -> provider != preferredProvider)
                .forEach(providers::add);
        return providers;
    }

    @FunctionalInterface
    public interface ProviderCall {
        String call(AiProvider provider);
    }
}
