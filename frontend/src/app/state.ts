import type { ChatResponse, ConversationMessageResponse, ConversationResponse, ExecutionDetailsResponse } from "../api/client";
import type { ProviderStepProps } from "../components";

export type MessageStatus = "loading" | "ready" | "error";

export interface ChatMessage {
  content: string;
  id: string;
  role: "user" | "assistant";
  status: MessageStatus;
}

export interface RunTrace {
  attemptCount: number;
  durationMs: number;
  fallbackUsed: boolean;
  model: string;
  provider: string;
  status: "SUCCESS" | "FAILED";
  steps: readonly ProviderStepProps[];
}

export interface Conversation {
  id: string;
  messages: readonly ChatMessage[];
  preview: string;
  run?: RunTrace;
  timestamp: string;
  title: string;
}

export interface PendingRequest {
  assistantMessageId: string;
  conversationId: string;
  prompt: string;
}

export interface AppState {
  conversations: readonly Conversation[];
  detailsState: "visible" | "hidden";
  draft: string;
  pendingRequest?: PendingRequest;
  selectedConversationId?: string;
  sidebarState: "expanded" | "collapsed";
}

export type AppAction =
  | { type: "toggle-sidebar" }
  | { type: "toggle-details" }
  | { type: "select-conversation"; conversationId: string }
  | { type: "set-draft"; value: string }
  | { type: "replace-conversations"; conversations: readonly Conversation[] }
  | { type: "new-conversation"; conversation: Conversation }
  | { type: "submit-prompt"; assistantMessageId: string; conversationId: string; userMessageId: string }
  | { type: "resolve-prompt"; response: ChatResponse }
  | { type: "reject-prompt"; message: string };

const fallbackAnswer =
  "The CAP theorem says a distributed system can fully guarantee only two of three things at the same time: consistency, availability, and tolerance to network partitions. Because network failures are unavoidable, systems usually choose whether consistency or availability matters more during a disruption.";

const fallbackTrace: RunTrace = {
  attemptCount: 2,
  durationMs: 1842,
  fallbackUsed: true,
  model: "claude-sonnet-4",
  provider: "Anthropic",
  status: "SUCCESS",
  steps: [
    { attemptCount: 1, durationMs: 716, message: "Upstream provider timed out.", provider: "OPENAI", status: "failed" },
    { attemptCount: 1, durationMs: 1126, message: "Fallback response returned.", provider: "ANTHROPIC", status: "success" },
    { message: "Not required after a successful response.", provider: "GEMINI", status: "skipped" },
  ],
};

export const initialAppState: AppState = {
  conversations: [],
  detailsState: "visible",
  draft: "",
  sidebarState: "expanded",
};

export const demoConversations: readonly Conversation[] = [
  {
    id: "fallback-demo",
    messages: [
      { content: "Explain the CAP theorem in simple terms.", id: "message-user-cap", role: "user", status: "ready" },
      { content: fallbackAnswer, id: "message-assistant-cap", role: "assistant", status: "ready" },
    ],
    preview: "The CAP theorem says…",
    run: fallbackTrace,
    timestamp: "Now",
    title: "Resilient fallback",
  },
  {
    id: "error-demo",
    messages: [
      { content: "Show me how a failed request is presented.", id: "message-user-error", role: "user", status: "ready" },
      { content: "No provider could complete this request. Please try again.", id: "message-assistant-error", role: "assistant", status: "error" },
    ],
    preview: "No provider could complete…",
    run: {
      attemptCount: 3,
      durationMs: 2410,
      fallbackUsed: true,
      model: "—",
      provider: "—",
      status: "FAILED",
      steps: [
        { durationMs: 810, message: "Request timed out.", provider: "OPENAI", status: "failed" },
        { durationMs: 904, message: "Service unavailable.", provider: "ANTHROPIC", status: "failed" },
        { durationMs: 696, message: "Quota exceeded.", provider: "GEMINI", status: "failed" },
      ],
    },
    timestamp: "Demo",
    title: "API error handling",
  },
];

function formatTimestamp(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "—";
  return new Intl.DateTimeFormat("en", { hour: "2-digit", minute: "2-digit" }).format(date);
}

function titleFrom(summary: ConversationResponse, messages: readonly ConversationMessageResponse[]): string {
  const firstUserMessage = messages.find((message) => message.role === "user");
  return summary.title?.trim() || firstUserMessage?.content.slice(0, 34) || "New conversation";
}

export function conversationFromApi(
  summary: ConversationResponse,
  messages: readonly ConversationMessageResponse[] = [],
): Conversation {
  const mappedMessages: readonly ChatMessage[] = messages.map((message) => ({
    content: message.content,
    id: message.id,
    role: message.role,
    status: "ready",
  }));
  return {
    id: summary.id,
    messages: mappedMessages,
    preview: mappedMessages.at(-1)?.content || "No messages yet",
    timestamp: formatTimestamp(summary.createdAt),
    title: titleFrom(summary, messages),
  };
}

export function runTraceFromApi(execution: ExecutionDetailsResponse): RunTrace {
  return {
    attemptCount: execution.attemptCount,
    durationMs: execution.durationMs,
    fallbackUsed: execution.fallbackUsed,
    model: execution.model,
    provider: execution.provider[0] + execution.provider.slice(1).toLowerCase(),
    status: execution.status,
    steps: execution.attempts.map((attempt) => ({
      attemptCount: attempt.attempt ?? undefined,
      durationMs: attempt.durationMs,
      provider: attempt.provider,
      status: attempt.status.toLowerCase() as ProviderStepProps["status"],
    })),
  };
}

export function createEmptyConversation(summary: ConversationResponse): Conversation {
  return conversationFromApi(summary);
}

function updateConversation(
  conversations: readonly Conversation[],
  id: string,
  update: (conversation: Conversation) => Conversation,
): readonly Conversation[] {
  return conversations.map((conversation) => (conversation.id === id ? update(conversation) : conversation));
}

export function appReducer(state: AppState, action: AppAction): AppState {
  switch (action.type) {
    case "toggle-sidebar":
      return { ...state, sidebarState: state.sidebarState === "expanded" ? "collapsed" : "expanded" };
    case "toggle-details":
      return { ...state, detailsState: state.detailsState === "visible" ? "hidden" : "visible" };
    case "select-conversation":
      return { ...state, selectedConversationId: action.conversationId };
    case "set-draft":
      return { ...state, draft: action.value };
    case "replace-conversations":
      return {
        ...state,
        conversations: action.conversations,
        draft: "",
        pendingRequest: undefined,
        selectedConversationId: action.conversations[0]?.id,
      };
    case "new-conversation":
      return {
        ...state,
        conversations: [action.conversation, ...state.conversations],
        draft: "",
        selectedConversationId: action.conversation.id,
      };
    case "submit-prompt": {
      const prompt = state.draft.trim();
      if (!prompt || state.pendingRequest) return state;
      const messages: readonly ChatMessage[] = [
        { content: prompt, id: action.userMessageId, role: "user", status: "ready" },
        { content: "Thinking…", id: action.assistantMessageId, role: "assistant", status: "loading" },
      ];
      return {
        ...state,
        conversations: updateConversation(state.conversations, action.conversationId, (conversation) => ({
          ...conversation,
          messages: [...conversation.messages, ...messages],
          preview: prompt,
          run: {
            attemptCount: 1,
            durationMs: 0,
            fallbackUsed: false,
            model: "Pending",
            provider: "OpenAI",
            status: "SUCCESS",
            steps: [{ message: "Waiting for provider response…", provider: "OPENAI", status: "running" }],
          },
          title: conversation.messages.length === 0 ? prompt.slice(0, 34) : conversation.title,
        })),
        draft: "",
        pendingRequest: { assistantMessageId: action.assistantMessageId, conversationId: action.conversationId, prompt },
        selectedConversationId: action.conversationId,
      };
    }
    case "resolve-prompt": {
      const pending = state.pendingRequest;
      if (!pending) return state;
      return {
        ...state,
        conversations: updateConversation(state.conversations, pending.conversationId, (conversation) => ({
          ...conversation,
          messages: conversation.messages.map((message) =>
            message.id === pending.assistantMessageId
              ? { content: action.response.message.content, id: action.response.message.id, role: "assistant", status: "ready" }
              : message,
          ),
          preview: action.response.message.content,
          run: runTraceFromApi(action.response.execution),
        })),
        pendingRequest: undefined,
      };
    }
    case "reject-prompt": {
      const pending = state.pendingRequest;
      if (!pending) return state;
      return {
        ...state,
        conversations: updateConversation(state.conversations, pending.conversationId, (conversation) => ({
          ...conversation,
          messages: conversation.messages.map((message) =>
            message.id === pending.assistantMessageId
              ? { ...message, content: action.message, status: "error" }
              : message,
          ),
          preview: action.message,
          run: undefined,
        })),
        pendingRequest: undefined,
      };
    }
  }
}

export function getSelectedConversation(state: AppState): Conversation | undefined {
  return state.conversations.find((conversation) => conversation.id === state.selectedConversationId);
}
