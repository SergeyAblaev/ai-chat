package com.example.api;

import com.example.multillm.AiProvider;
import com.example.multillm.AiProviderRouter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public record ExecutionDetails(
        String requestId,
        ExecutionMode mode,
        AiProvider provider,
        String model,
        ExecutionStatus status,
        boolean fallbackUsed,
        int attemptCount,
        long durationMs,
        List<AttemptExecution> attempts
) {

    public static ExecutionDetails success(
            ExecutionMode mode,
            AiProviderRouter.RoutingResult result,
            String model
    ) {
        List<AttemptExecution> attempts = new ArrayList<>();
        Set<AiProvider> attemptedProviders = EnumSet.noneOf(AiProvider.class);

        for (AiProviderRouter.Attempt attempt : result.attempts()) {
            attemptedProviders.add(attempt.provider());
            attempts.add(new AttemptExecution(
                    attempt.provider(),
                    attempt.attempt(),
                    attempt.status(),
                    attempt.durationMs()
            ));
        }
        for (AiProvider provider : AiProvider.values()) {
            if (!attemptedProviders.contains(provider)) {
                attempts.add(new AttemptExecution(
                        provider,
                        null,
                        AiProviderRouter.AttemptStatus.SKIPPED,
                        0
                ));
            }
        }

        return new ExecutionDetails(
                ApiIds.next("req"),
                mode,
                result.provider(),
                model,
                ExecutionStatus.SUCCESS,
                result.fallbackUsed(),
                result.attemptCount(),
                result.durationMs(),
                List.copyOf(attempts)
        );
    }
}
