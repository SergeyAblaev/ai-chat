import { describe, expect, it, vi } from "vitest";
import { ApiClient, ApiError, type ChatResponse } from "./client";

const credentials = { password: "secret", username: "user" };

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), { headers: { "Content-Type": "application/json" }, status });
}

describe("ApiClient", () => {
  it("uses runtime Basic credentials and the server conversation id", async () => {
    const fetcher = vi.fn<typeof fetch>()
      .mockResolvedValueOnce(jsonResponse({ createdAt: "2026-08-15T10:00:00Z", id: "conv/server id", title: null }, 201))
      .mockResolvedValueOnce(jsonResponse({ execution: {}, message: { content: "Answer" } }));
    const client = new ApiClient({ baseUrl: "/api", credentials, fetcher });

    const conversation = await client.createConversation();
    await client.addConversationMessage(conversation.id, "Question");

    expect(fetcher).toHaveBeenNthCalledWith(1, "/api/conversations", expect.objectContaining({ method: "POST" }));
    expect(fetcher).toHaveBeenNthCalledWith(
      2,
      "/api/conversations/conv%2Fserver%20id/messages",
      expect.objectContaining({ body: JSON.stringify({ prompt: "Question" }), method: "POST" }),
    );
    const firstHeaders = fetcher.mock.calls[0]?.[1]?.headers as Record<string, string>;
    expect(firstHeaders.Authorization).toMatch(/^Basic /);
    expect(firstHeaders.Authorization).not.toContain("secret");
  });

  it("covers list, history, delete, and resilient chat contracts", async () => {
    const chat: ChatResponse = {
      execution: {
        attemptCount: 1,
        attempts: [{ attempt: 1, durationMs: 10, provider: "OPENAI", status: "SUCCESS" }],
        durationMs: 10,
        fallbackUsed: false,
        mode: "RESILIENT",
        model: "gpt-5-mini",
        provider: "OPENAI",
        requestId: "req_1",
        status: "SUCCESS",
      },
      message: { content: "Answer", createdAt: "2026-08-15T10:01:00Z", id: "msg_1", role: "assistant" },
    };
    const fetcher = vi.fn<typeof fetch>()
      .mockResolvedValueOnce(jsonResponse([]))
      .mockResolvedValueOnce(jsonResponse([]))
      .mockResolvedValueOnce(new Response(null, { status: 204 }))
      .mockResolvedValueOnce(jsonResponse(chat));
    const client = new ApiClient({ credentials, fetcher });

    await client.listConversations();
    await client.listMessages("conv_1");
    await client.deleteConversation("conv_1");
    await client.resilientChat("Hello");

    expect(fetcher.mock.calls.map(([url]) => url)).toEqual([
      "/api/conversations",
      "/api/conversations/conv_1/messages",
      "/api/conversations/conv_1",
      "/api/chatbot/chat",
    ]);
  });

  it("maps backend authentication errors to a user-facing error", async () => {
    const fetcher = vi.fn<typeof fetch>().mockResolvedValue(jsonResponse({ error: "Unauthorized" }, 401));
    const client = new ApiClient({ credentials, fetcher });

    await expect(client.listConversations()).rejects.toEqual(
      new ApiError("Authentication failed. Check your username and password.", 401),
    );
  });
});
