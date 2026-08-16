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

    public RoutingResult chat(ProviderCall providerCall) {
        long requestStartedAt = System.nanoTime();
        AiProvider preferredProvider = successfulProvider.get();
        AiProvider retryProvider = preferredProvider == null
                ? AiProvider.OPENAI
                : preferredProvider;
        Exception lastException = null;
        List<Attempt> attempts = new ArrayList<>();

        for (AiProvider provider : providersStartingWith(preferredProvider)) {
            int maxAttempts = provider == retryProvider
                    ? PREFERRED_PROVIDER_MAX_ATTEMPTS
                    : 1;

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                long attemptStartedAt = System.nanoTime();
                try {
                    log.debug("Attempting to process prompt with {}. Attempt #{}",
                            provider, attempt);
                    String response = providerCall.call(provider);
                    attempts.add(new Attempt(
                            provider,
                            attempt,
                            AttemptStatus.SUCCESS,
                            elapsedMillis(attemptStartedAt)
                    ));
                    successfulProvider.set(provider);
                    log.info("Setted successfulProvider is {}", provider);
                    return new RoutingResult(
                            response,
                            provider,
                            elapsedMillis(requestStartedAt),
                            List.copyOf(attempts)
                    );
                } catch (DataAccessException exception) {
                    throw exception;
                } catch (Exception exception) {
                    attempts.add(new Attempt(
                            provider,
                            attempt,
                            AttemptStatus.FAILED,
                            elapsedMillis(attemptStartedAt)
                    ));
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

    private long elapsedMillis(long startedAt) {
        return Math.max(0L, (System.nanoTime() - startedAt) / 1_000_000L);
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

    public record RoutingResult(
            String content,
            AiProvider provider,
            long durationMs,
            List<Attempt> attempts
    ) {
        public int attemptCount() {
            return attempts.size();
        }

        public boolean fallbackUsed() {
            return attempts.stream()
                    .map(Attempt::provider)
                    .distinct()
                    .limit(2)
                    .count() > 1;
        }
    }

    public record Attempt(
            AiProvider provider,
            int attempt,
            AttemptStatus status,
            long durationMs
    ) {}

    public enum AttemptStatus {
        PENDING,
        RUNNING,
        SUCCESS,
        FAILED,
        SKIPPED
    }
}
