import { describe, expect, it } from "vitest";
import type { ChatResponse, ConversationResponse } from "../api/client";
import {
  appReducer,
  conversationFromApi,
  createEmptyConversation,
  demoConversations,
  getSelectedConversation,
  initialAppState,
  runTraceFromApi,
} from "./state";

const summary: ConversationResponse = {
  createdAt: "2026-08-15T10:00:00Z",
  id: "conv_server",
  title: null,
};

const response: ChatResponse = {
  execution: {
    attemptCount: 2,
    attempts: [
      { attempt: 1, durationMs: 20, provider: "OPENAI", status: "FAILED" },
      { attempt: 1, durationMs: 30, provider: "ANTHROPIC", status: "SUCCESS" },
      { attempt: null, durationMs: 0, provider: "GEMINI", status: "SKIPPED" },
    ],
    durationMs: 50,
    fallbackUsed: true,
    mode: "CONTEXT",
    model: "claude-sonnet",
    provider: "ANTHROPIC",
    requestId: "req_server",
    status: "SUCCESS",
  },
  message: {
    content: "Backend answer",
    createdAt: "2026-08-15T10:01:00Z",
    id: "msg_server",
    role: "assistant",
  },
};

describe("application API state", () => {
  it("hydrates server conversations and derives a title from history", () => {
    const conversation = conversationFromApi(summary, [
      { content: "Server question", createdAt: summary.createdAt, id: "msg_user", role: "user" },
    ]);
    const state = appReducer(initialAppState, { conversations: [conversation], type: "replace-conversations" });

    expect(getSelectedConversation(state)?.id).toBe("conv_server");
    expect(getSelectedConversation(state)?.title).toBe("Server question");
  });

  it("creates and selects a conversation using the server id", () => {
    const state = appReducer(initialAppState, {
      conversation: createEmptyConversation(summary),
      type: "new-conversation",
    });

    expect(state.selectedConversationId).toBe("conv_server");
    expect(getSelectedConversation(state)?.messages).toHaveLength(0);
  });

  it("moves a prompt through loading and the actual API response", () => {
    const base = appReducer(initialAppState, {
      conversations: [createEmptyConversation(summary)],
      type: "replace-conversations",
    });
    const withDraft = appReducer(base, { type: "set-draft", value: "A server question" });
    const loading = appReducer(withDraft, {
      assistantMessageId: "pending-message",
      conversationId: "conv_server",
      type: "submit-prompt",
      userMessageId: "user-message",
    });
    const resolved = appReducer(loading, { response, type: "resolve-prompt" });
    const conversation = getSelectedConversation(resolved);

    expect(loading.pendingRequest?.conversationId).toBe("conv_server");
    expect(conversation?.messages.at(-1)?.id).toBe("msg_server");
    expect(conversation?.run?.fallbackUsed).toBe(true);
    expect(conversation?.run?.steps.map((step) => step.status)).toEqual(["failed", "success", "skipped"]);
  });

  it("turns a rejected API request into an understandable message state", () => {
    const base = appReducer(initialAppState, {
      conversations: [createEmptyConversation(summary)],
      type: "replace-conversations",
    });
    const loading = appReducer(appReducer(base, { type: "set-draft", value: "Question" }), {
      assistantMessageId: "pending-message",
      conversationId: "conv_server",
      type: "submit-prompt",
      userMessageId: "user-message",
    });
    const rejected = appReducer(loading, { message: "Backend is unavailable.", type: "reject-prompt" });

    expect(getSelectedConversation(rejected)?.messages.at(-1)?.status).toBe("error");
    expect(getSelectedConversation(rejected)?.messages.at(-1)?.content).toBe("Backend is unavailable.");
  });

  it("maps execution metadata and preserves the offline D05 fixture", () => {
    const trace = runTraceFromApi(response.execution);

    expect(trace.provider).toBe("Anthropic");
    expect(trace.attemptCount).toBe(2);
    expect(demoConversations[0]?.run?.fallbackUsed).toBe(true);
  });
});
