package com.example.api;

import java.time.OffsetDateTime;

public record AssistantMessage(
        String id,
        String role,
        String content,
        OffsetDateTime createdAt
) {
    public static AssistantMessage create(String content) {
        return new AssistantMessage(
                ApiIds.next("msg"),
                "assistant",
                content,
                OffsetDateTime.now()
        );
    }
}
