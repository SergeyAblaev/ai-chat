import { useEffect, useReducer } from "react";
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
import { appReducer, createEmptyConversation, getSelectedConversation, initialAppState } from "./state";

const icon = (glyph: string) => <span aria-hidden="true" className={styles.icon}>{glyph}</span>;

export function App() {
  const [state, dispatch] = useReducer(appReducer, initialAppState);
  const conversation = getSelectedConversation(state);

  useEffect(() => {
    if (!state.pendingRequest) return;
    const timeout = window.setTimeout(() => dispatch({ type: "resolve-prompt" }), 900);
    return () => window.clearTimeout(timeout);
  }, [state.pendingRequest]);

  const newConversation = () => {
    const id = `conversation-${Date.now()}`;
    dispatch({ conversation: createEmptyConversation(id), type: "new-conversation" });
  };

  const submitPrompt = () => {
    const suffix = `${Date.now()}`;
    dispatch({ assistantMessageId: `assistant-${suffix}`, type: "submit-prompt", userMessageId: `user-${suffix}` });
  };

  const details = conversation.run ? (
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
      <p>Details will appear after the first response.</p>
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
          footer={<span className={styles.environment}>Local scenario</span>}
          newConversationIcon={icon("+")}
          onNewConversation={newConversation}
          onSelectConversation={(conversationId) => dispatch({ conversationId, type: "select-conversation" })}
          selectedConversationId={conversation.id}
          state={state.sidebarState}
        />
      }
      sidebarState={state.sidebarState}
      topBar={
        <TopBar
          actions={
            <PrimaryGlassButton leadingIcon={icon(state.detailsState === "visible" ? "◧" : "▣")} onClick={() => dispatch({ type: "toggle-details" })}>
              {state.detailsState === "visible" ? "Hide details" : "Show details"}
            </PrimaryGlassButton>
          }
          leading={
            <GlassIconButton
              icon={icon(state.sidebarState === "expanded" ? "←" : "→")}
              label={state.sidebarState === "expanded" ? "Collapse sidebar" : "Expand sidebar"}
              onClick={() => dispatch({ type: "toggle-sidebar" })}
              size="sm"
            />
          }
          title={conversation.title}
        />
      }
    >
      <MessageViewport emptyState={<div className={styles.welcome}><span className={styles.welcomeMark}>AI</span><h1>How can I help?</h1><p>Start a new resilient conversation.</p></div>}>
        {conversation.messages.length > 0
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
          disabled={Boolean(state.pendingRequest)}
          onChange={(value) => dispatch({ type: "set-draft", value })}
          onSubmit={submitPrompt}
          placeholder="Message AI Console…"
          sendIcon={icon("↑")}
          sending={Boolean(state.pendingRequest)}
          value={state.draft}
        />
        <p className={styles.composerHint}>Enter to send · Shift + Enter for a new line · Use /error to preview failure</p>
      </div>
    </AppShell>
  );
}
