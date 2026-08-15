import { describe, expect, it } from "vitest";

import { API_BASE_URL, apiUrl } from "./config";

describe("apiUrl", () => {
  it("joins API paths without duplicate separators", () => {
    expect(apiUrl("conversations")).toBe(`${API_BASE_URL}/conversations`);
    expect(apiUrl("/chatbot/chat")).toBe(`${API_BASE_URL}/chatbot/chat`);
  });
});
