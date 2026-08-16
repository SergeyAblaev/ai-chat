package com.example.api;

import com.example.multillm.AiProvider;
import com.example.multillm.AiProviderRouter;

public record AttemptExecution(
        AiProvider provider,
        Integer attempt,
        AiProviderRouter.AttemptStatus status,
        long durationMs
) {}
