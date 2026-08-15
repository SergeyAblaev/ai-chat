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

interface PendingRequest {
  assistantMessageId: string;
  conversationId: string;
  prompt: string;
}

export interface AppState {
  conversations: readonly Conversation[];
  detailsState: "visible" | "hidden";
  draft: string;
  pendingRequest?: PendingRequest;
  selectedConversationId: string;
  sidebarState: "expanded" | "collapsed";
}

export type AppAction =
  | { type: "toggle-sidebar" }
  | { type: "toggle-details" }
  | { type: "select-conversation"; conversationId: string }
  | { type: "set-draft"; value: string }
  | { type: "new-conversation"; conversation: Conversation }
  | { type: "submit-prompt"; assistantMessageId: string; userMessageId: string }
  | { type: "resolve-prompt" };

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
    {
      attemptCount: 1,
      durationMs: 716,
      message: "Upstream provider timed out. Fallback started automatically.",
      provider: "OPENAI",
      status: "failed",
    },
    {
      attemptCount: 1,
      durationMs: 1126,
      message: "Response returned by the fallback provider.",
      provider: "ANTHROPIC",
      status: "success",
    },
    {
      message: "Not required after a successful response.",
      provider: "GEMINI",
      status: "skipped",
    },
  ],
};

export const initialAppState: AppState = {
  conversations: [
    {
      id: "fallback-demo",
      messages: [
        {
          content: "Explain the CAP theorem in simple terms.",
          id: "message-user-cap",
          role: "user",
          status: "ready",
        },
        {
          content: fallbackAnswer,
          id: "message-assistant-cap",
          role: "assistant",
          status: "ready",
        },
      ],
      preview: "The CAP theorem says…",
      run: fallbackTrace,
      timestamp: "Now",
      title: "Resilient fallback",
    },
    {
      id: "error-demo",
      messages: [
        {
          content: "Show me how a failed request is presented.",
          id: "message-user-error",
          role: "user",
          status: "ready",
        },
        {
          content: "No provider could complete this request. Please try again.",
          id: "message-assistant-error",
          role: "assistant",
          status: "error",
        },
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
      timestamp: "12m",
      title: "API error handling",
    },
  ],
  detailsState: "visible",
  draft: "",
  selectedConversationId: "fallback-demo",
  sidebarState: "expanded",
};

export function createEmptyConversation(id: string): Conversation {
  return {
    id,
    messages: [],
    preview: "No messages yet",
    timestamp: "Now",
    title: "New conversation",
  };
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
        conversations: updateConversation(state.conversations, state.selectedConversationId, (conversation) => ({
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
        pendingRequest: {
          assistantMessageId: action.assistantMessageId,
          conversationId: state.selectedConversationId,
          prompt,
        },
      };
    }
    case "resolve-prompt": {
      const pending = state.pendingRequest;
      if (!pending) return state;
      const failed = pending.prompt.toLowerCase().includes("/error");
      const response = failed
        ? "No provider could complete this request. Please try again."
        : `Here is a resilient response to “${pending.prompt}”. The primary provider failed, so the request was completed by the configured fallback.`;
      const run = failed
        ? initialAppState.conversations[1].run
        : fallbackTrace;

      return {
        ...state,
        conversations: updateConversation(state.conversations, pending.conversationId, (conversation) => ({
          ...conversation,
          messages: conversation.messages.map((message) =>
            message.id === pending.assistantMessageId
              ? { ...message, content: response, status: failed ? "error" : "ready" }
              : message,
          ),
          preview: response,
          run,
        })),
        pendingRequest: undefined,
      };
    }
  }
}

export function getSelectedConversation(state: AppState): Conversation {
  return state.conversations.find((conversation) => conversation.id === state.selectedConversationId) ?? state.conversations[0];
}
