import { describe, expect, it } from "vitest";
import { appReducer, createEmptyConversation, getSelectedConversation, initialAppState } from "./state";

describe("application scenario state", () => {
  it("toggles shell variants and selects a conversation", () => {
    const collapsed = appReducer(initialAppState, { type: "toggle-sidebar" });
    const hidden = appReducer(collapsed, { type: "toggle-details" });
    const selected = appReducer(hidden, { conversationId: "error-demo", type: "select-conversation" });

    expect(selected.sidebarState).toBe("collapsed");
    expect(selected.detailsState).toBe("hidden");
    expect(getSelectedConversation(selected).id).toBe("error-demo");
  });

  it("creates and selects an empty conversation", () => {
    const state = appReducer(initialAppState, {
      conversation: createEmptyConversation("new-id"),
      type: "new-conversation",
    });

    expect(state.selectedConversationId).toBe("new-id");
    expect(getSelectedConversation(state).messages).toHaveLength(0);
  });

  it("moves a prompt through loading and fallback success", () => {
    const withDraft = appReducer(initialAppState, { type: "set-draft", value: "A new question" });
    const loading = appReducer(withDraft, {
      assistantMessageId: "assistant-new",
      type: "submit-prompt",
      userMessageId: "user-new",
    });
    const resolved = appReducer(loading, { type: "resolve-prompt" });
    const conversation = getSelectedConversation(resolved);

    expect(loading.pendingRequest).toBeDefined();
    expect(conversation.messages.at(-1)?.status).toBe("ready");
    expect(conversation.run?.fallbackUsed).toBe(true);
    expect(conversation.run?.steps.map((step) => step.status)).toEqual(["failed", "success", "skipped"]);
  });

  it("resolves the deterministic error scenario", () => {
    const withDraft = appReducer(initialAppState, { type: "set-draft", value: "/error please" });
    const loading = appReducer(withDraft, {
      assistantMessageId: "assistant-error",
      type: "submit-prompt",
      userMessageId: "user-error",
    });
    const resolved = appReducer(loading, { type: "resolve-prompt" });

    expect(getSelectedConversation(resolved).messages.at(-1)?.status).toBe("error");
    expect(getSelectedConversation(resolved).run?.status).toBe("FAILED");
  });
});
