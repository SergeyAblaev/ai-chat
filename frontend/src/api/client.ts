import { API_BASE_URL } from "./config";

export type ApiProvider = "OPENAI" | "ANTHROPIC" | "GEMINI";
export type ApiAttemptStatus = "SUCCESS" | "FAILED" | "SKIPPED" | "RUNNING";

export interface BasicCredentials {
  password: string;
  username: string;
}

export interface ConversationResponse {
  createdAt: string;
  id: string;
  title: string | null;
}

export interface ConversationMessageResponse {
  content: string;
  createdAt: string;
  id: string;
  role: "user" | "assistant";
}

export interface AssistantMessageResponse extends ConversationMessageResponse {
  role: "assistant";
}

export interface AttemptExecutionResponse {
  attempt: number | null;
  durationMs: number;
  provider: ApiProvider;
  status: ApiAttemptStatus;
}

export interface ExecutionDetailsResponse {
  attemptCount: number;
  attempts: readonly AttemptExecutionResponse[];
  durationMs: number;
  fallbackUsed: boolean;
  mode: "CONTEXT" | "RESILIENT";
  model: string;
  provider: ApiProvider;
  requestId: string;
  status: "SUCCESS" | "FAILED";
}

export interface ChatResponse {
  execution: ExecutionDetailsResponse;
  message: AssistantMessageResponse;
}

export class ApiError extends Error {
  readonly status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

interface ApiClientOptions {
  baseUrl?: string;
  credentials: BasicCredentials;
  fetcher?: typeof fetch;
}

function basicAuthorization({ username, password }: BasicCredentials): string {
  const bytes = new TextEncoder().encode(`${username}:${password}`);
  let binary = "";
  for (const byte of bytes) binary += String.fromCharCode(byte);
  return `Basic ${btoa(binary)}`;
}

function errorMessage(status: number, payload: unknown): string {
  if (status === 401) return "Authentication failed. Check your username and password.";
  if (status === 403) return "This account does not have permission for that action.";

  if (payload && typeof payload === "object") {
    const problem = payload as Record<string, unknown>;
    for (const key of ["detail", "message", "error"]) {
      if (typeof problem[key] === "string" && problem[key]) return problem[key];
    }
  }
  if (typeof payload === "string" && payload.trim()) return payload;
  return `Request failed with status ${status}.`;
}

export class ApiClient {
  private readonly authorization: string;
  private readonly baseUrl: string;
  private readonly fetcher: typeof fetch;

  constructor({ baseUrl = API_BASE_URL, credentials, fetcher = fetch }: ApiClientOptions) {
    this.authorization = basicAuthorization(credentials);
    this.baseUrl = baseUrl.replace(/\/$/, "");
    this.fetcher = fetcher;
  }

  private async request<T>(path: string, init: RequestInit = {}): Promise<T> {
    const response = await this.fetcher(`${this.baseUrl}${path}`, {
      ...init,
      headers: {
        Accept: "application/json",
        Authorization: this.authorization,
        ...(init.body ? { "Content-Type": "application/json" } : {}),
        ...init.headers,
      },
    });

    if (!response.ok) {
      const contentType = response.headers.get("content-type") ?? "";
      const payload: unknown = contentType.includes("application/json")
        ? await response.json().catch(() => undefined)
        : await response.text().catch(() => undefined);
      throw new ApiError(errorMessage(response.status, payload), response.status);
    }

    if (response.status === 204) return undefined as T;
    return await response.json() as T;
  }

  createConversation(): Promise<ConversationResponse> {
    return this.request("/conversations", { method: "POST" });
  }

  listConversations(): Promise<readonly ConversationResponse[]> {
    return this.request("/conversations");
  }

  listMessages(conversationId: string): Promise<readonly ConversationMessageResponse[]> {
    return this.request(`/conversations/${encodeURIComponent(conversationId)}/messages`);
  }

  deleteConversation(conversationId: string): Promise<void> {
    return this.request(`/conversations/${encodeURIComponent(conversationId)}`, { method: "DELETE" });
  }

  addConversationMessage(conversationId: string, prompt: string): Promise<ChatResponse> {
    return this.request(`/conversations/${encodeURIComponent(conversationId)}/messages`, {
      body: JSON.stringify({ prompt }),
      method: "POST",
    });
  }

  resilientChat(prompt: string): Promise<ChatResponse> {
    return this.request("/chatbot/chat", {
      body: JSON.stringify({ prompt }),
      method: "POST",
    });
  }
}
