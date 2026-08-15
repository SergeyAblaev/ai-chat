import { useEffect, useMemo, useReducer, useState, type ComponentProps } from "react";
import { ApiClient, ApiError, type BasicCredentials, type ChatResponse } from "../api/client";
import {
  AppShell,
  AssistantMessageCard,
  GlassIconButton,
  MessageViewport,
  NavigationSidebar,
  PrimaryGlassButton,
  PromptComposer,
  RunDetailsPanel,
  TopBar,
  UserMessageBubble,
} from "../components";
import styles from "./App.module.css";
import {
  appReducer,
  conversationFromApi,
  createEmptyConversation,
  demoConversations,
  getSelectedConversation,
  initialAppState,
} from "./state";

type AppMode = "login" | "api" | "demo";

const icon = (glyph: string) => <span aria-hidden="true" className={styles.icon}>{glyph}</span>;

function readableError(error: unknown): string {
  if (error instanceof ApiError || error instanceof Error) return error.message;
  return "The request could not be completed. Please try again.";
}

function demoResponse(prompt: string): ChatResponse {
  return {
    execution: {
      attemptCount: 2,
      attempts: [
        { attempt: 1, durationMs: 716, provider: "OPENAI", status: "FAILED" },
        { attempt: 1, durationMs: 1126, provider: "ANTHROPIC", status: "SUCCESS" },
        { attempt: null, durationMs: 0, provider: "GEMINI", status: "SKIPPED" },
      ],
      durationMs: 1842,
      fallbackUsed: true,
      mode: "RESILIENT",
      model: "claude-sonnet-4",
      provider: "ANTHROPIC",
      requestId: "req_demo",
      status: "SUCCESS",
    },
    message: {
      content: `Here is a resilient response to “${prompt}”. The primary provider failed, so the configured fallback completed the request.`,
      createdAt: new Date().toISOString(),
      id: `assistant-demo-${Date.now()}`,
      role: "assistant",
    },
  };
}

interface LoginPanelProps {
  busy: boolean;
  error?: string;
  onConnect: (credentials: BasicCredentials) => Promise<void>;
  onDemo: () => void;
}

function LoginPanel({ busy, error, onConnect, onDemo }: LoginPanelProps) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const submit: ComponentProps<"form">["onSubmit"] = (event) => {
    event.preventDefault();
    void onConnect({ password, username });
  };

  return (
    <main className={styles.loginPage}>
      <section className={styles.loginCard}>
        <span className={styles.welcomeMark}>AI</span>
        <div>
          <p className={styles.eyebrow}>AI Chat Console</p>
          <h1>Connect to the backend</h1>
          <p className={styles.loginCopy}>Use an account configured by the Spring Boot application.</p>
        </div>
        <form className={styles.loginForm} onSubmit={submit}>
          <label>
            <span>Username</span>
            <input autoComplete="username" disabled={busy} onChange={(event) => setUsername(event.target.value)} required value={username} />
          </label>
          <label>
            <span>Password</span>
            <input autoComplete="current-password" disabled={busy} onChange={(event) => setPassword(event.target.value)} required type="password" value={password} />
          </label>
          {error && <p className={styles.authError} role="alert">{error}</p>}
          <button className={styles.connectButton} disabled={busy} type="submit">
            {busy ? "Connecting…" : "Connect to backend"}
          </button>
        </form>
        <button className={styles.demoButton} disabled={busy} onClick={onDemo} type="button">Explore D05 demo</button>
        <p className={styles.securityNote}>Credentials stay in memory for this tab and are never stored.</p>
      </section>
    </main>
  );
}

export function App() {
  const [state, dispatch] = useReducer(appReducer, initialAppState);
  const [mode, setMode] = useState<AppMode>("login");
  const [credentials, setCredentials] = useState<BasicCredentials>();
  const [busy, setBusy] = useState(false);
  const [operationError, setOperationError] = useState<string>();
  const client = useMemo(() => (credentials ? new ApiClient({ credentials }) : undefined), [credentials]);
  const conversation = getSelectedConversation(state);

  useEffect(() => {
    const pending = state.pendingRequest;
    if (!pending) return;

    if (mode === "demo") {
      const timeout = window.setTimeout(() => {
        if (pending.prompt.toLowerCase().includes("/error")) {
          dispatch({ message: "No provider could complete this demo request. Please try again.", type: "reject-prompt" });
        } else {
          dispatch({ response: demoResponse(pending.prompt), type: "resolve-prompt" });
        }
      }, 700);
      return () => window.clearTimeout(timeout);
    }

    if (mode === "api" && client) {
      let active = true;
      void client.addConversationMessage(pending.conversationId, pending.prompt)
        .then((response) => {
          if (active) dispatch({ response, type: "resolve-prompt" });
        })
        .catch((error: unknown) => {
          if (active) dispatch({ message: readableError(error), type: "reject-prompt" });
        });
      return () => {
        active = false;
      };
    }
  }, [client, mode, state.pendingRequest]);

  const connect = async (nextCredentials: BasicCredentials) => {
    setBusy(true);
    setOperationError(undefined);
    try {
      const nextClient = new ApiClient({ credentials: nextCredentials });
      const summaries = await nextClient.listConversations();
      const conversations = await Promise.all(
        summaries.map(async (summary) => conversationFromApi(summary, await nextClient.listMessages(summary.id))),
      );
      setCredentials(nextCredentials);
      dispatch({ conversations, type: "replace-conversations" });
      setMode("api");
    } catch (error) {
      setOperationError(readableError(error));
    } finally {
      setBusy(false);
    }
  };

  const enterDemo = () => {
    dispatch({ conversations: demoConversations, type: "replace-conversations" });
    setMode("demo");
    setOperationError(undefined);
  };

  const disconnect = () => {
    setCredentials(undefined);
    setMode("login");
    setOperationError(undefined);
    dispatch({ conversations: [], type: "replace-conversations" });
  };

  const newConversation = async () => {
    setOperationError(undefined);
    if (mode === "demo") {
      dispatch({
        conversation: createEmptyConversation({ createdAt: new Date().toISOString(), id: `demo-${Date.now()}`, title: null }),
        type: "new-conversation",
      });
      return;
    }
    if (!client) return;
    setBusy(true);
    try {
      dispatch({ conversation: createEmptyConversation(await client.createConversation()), type: "new-conversation" });
    } catch (error) {
      setOperationError(readableError(error));
    } finally {
      setBusy(false);
    }
  };

  const submitPrompt = async () => {
    setOperationError(undefined);
    let targetConversation = conversation;
    if (!targetConversation) {
      if (mode === "api" && client) {
        try {
          targetConversation = createEmptyConversation(await client.createConversation());
        } catch (error) {
          setOperationError(readableError(error));
          return;
        }
      } else {
        targetConversation = createEmptyConversation({ createdAt: new Date().toISOString(), id: `demo-${Date.now()}`, title: null });
      }
      dispatch({ conversation: targetConversation, type: "new-conversation" });
    }
    const suffix = `${Date.now()}`;
    dispatch({
      assistantMessageId: `pending-${suffix}`,
      conversationId: targetConversation.id,
      type: "submit-prompt",
      userMessageId: `user-${suffix}`,
    });
  };

  if (mode === "login") {
    return <LoginPanel busy={busy} error={operationError} onConnect={connect} onDemo={enterDemo} />;
  }

  const details = conversation?.run ? (
    <RunDetailsPanel
      details={[
        { label: "Status", value: <span className={conversation.run.status === "FAILED" ? styles.failed : styles.succeeded}>{conversation.run.status}</span> },
        { label: "Fallback", value: conversation.run.fallbackUsed ? "Used" : "Not used" },
        { label: "Provider", value: conversation.run.provider },
        { label: "Model", value: conversation.run.model },
        { label: "Duration", value: `${conversation.run.durationMs} ms` },
        { label: "Attempts", value: conversation.run.attemptCount },
      ]}
      steps={conversation.run.steps}
    />
  ) : (
    <aside className={styles.emptyDetails}>
      <h2>Run details</h2>
      <p>Details will appear after the next response.</p>
    </aside>
  );

  return (
    <AppShell
      details={details}
      detailsState={state.detailsState}
      sidebar={
        <NavigationSidebar
          brand="AI Console"
          brandMark={<span className={styles.brandMark}>AI</span>}
          conversations={state.conversations}
          footer={<span className={styles.environment}>{mode === "api" ? "Backend connected" : "D05 demo · offline"}</span>}
          newConversationIcon={icon("+")}
          onNewConversation={() => void newConversation()}
          onSelectConversation={(conversationId) => dispatch({ conversationId, type: "select-conversation" })}
          selectedConversationId={conversation?.id}
          state={state.sidebarState}
        />
      }
      sidebarState={state.sidebarState}
      topBar={
        <TopBar
          actions={
            <>
              <PrimaryGlassButton leadingIcon={icon(state.detailsState === "visible" ? "◧" : "▣")} onClick={() => dispatch({ type: "toggle-details" })}>
                {state.detailsState === "visible" ? "Hide details" : "Show details"}
              </PrimaryGlassButton>
              <PrimaryGlassButton onClick={disconnect}>{mode === "api" ? "Disconnect" : "Exit demo"}</PrimaryGlassButton>
            </>
          }
          leading={
            <GlassIconButton
              icon={icon(state.sidebarState === "expanded" ? "←" : "→")}
              label={state.sidebarState === "expanded" ? "Collapse sidebar" : "Expand sidebar"}
              onClick={() => dispatch({ type: "toggle-sidebar" })}
              size="sm"
            />
          }
          title={conversation?.title ?? "New conversation"}
        />
      }
    >
      {operationError && <div className={styles.operationError} role="alert">{operationError}</div>}
      <MessageViewport emptyState={<div className={styles.welcome}><span className={styles.welcomeMark}>AI</span><h1>How can I help?</h1><p>Start a resilient conversation backed by the API.</p></div>}>
        {conversation?.messages.length
          ? conversation.messages.map((message) =>
              message.role === "user" ? (
                <UserMessageBubble key={message.id}>{message.content}</UserMessageBubble>
              ) : (
                <AssistantMessageCard key={message.id} pending={message.status === "loading"}>
                  <span className={message.status === "error" ? styles.errorMessage : undefined}>{message.content}</span>
                </AssistantMessageCard>
              ),
            )
          : undefined}
      </MessageViewport>
      <div className={styles.composerDock}>
        <PromptComposer
          disabled={busy || Boolean(state.pendingRequest)}
          onChange={(value) => dispatch({ type: "set-draft", value })}
          onSubmit={() => void submitPrompt()}
          placeholder="Message AI Console…"
          sendIcon={icon("↑")}
          sending={Boolean(state.pendingRequest)}
          value={state.draft}
        />
        <p className={styles.composerHint}>Enter to send · Shift + Enter for a new line{mode === "demo" ? " · Use /error to preview failure" : ""}</p>
      </div>
    </AppShell>
  );
}
