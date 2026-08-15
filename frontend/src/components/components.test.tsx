import { renderToStaticMarkup } from "react-dom/server";
import { describe, expect, it, vi } from "vitest";
import {
  AppShell,
  AssistantMessageCard,
  ConversationRow,
  GlassIconButton,
  MessageViewport,
  NavigationSidebar,
  PrimaryGlassButton,
  PrimarySendButton,
  PromptComposer,
  ProviderStep,
  RunDetailsPanel,
  TopBar,
  UserMessageBubble,
} from ".";

describe("stage 4 component library", () => {
  it("renders typed button variants", () => {
    const markup = renderToStaticMarkup(
      <div>
        <GlassIconButton icon="☰" label="Menu" size="sm" visualState="pressed" />
        <PrimaryGlassButton leadingIcon="+">New conversation</PrimaryGlassButton>
        <PrimarySendButton icon="↑" label="Send message" />
      </div>,
    );

    expect(markup).toContain('data-size="sm"');
    expect(markup).toContain('data-state="pressed"');
    expect(markup).toContain("New conversation");
    expect(markup).toContain('aria-label="Send message"');
  });

  it("renders navigation and selected conversation variants", () => {
    const conversations = [{ id: "one", title: "Design review", preview: "Latest message", timestamp: "10:42" }];
    const expanded = renderToStaticMarkup(
      <NavigationSidebar conversations={conversations} selectedConversationId="one" state="expanded" />,
    );
    const collapsed = renderToStaticMarkup(
      <NavigationSidebar conversations={conversations} state="collapsed" />,
    );
    const row = renderToStaticMarkup(<ConversationRow {...conversations[0]} selected />);

    expect(expanded).toContain('data-state="expanded"');
    expect(expanded).toContain('aria-current="page"');
    expect(collapsed).toContain('data-state="collapsed"');
    expect(row).toContain("Latest message");
  });

  it.each(["failed", "success", "skipped", "running"] as const)("renders the %s provider state", (status) => {
    const markup = renderToStaticMarkup(
      <ProviderStep attemptCount={2} durationMs={320} message="Provider result" provider="OPENAI" status={status} />,
    );

    expect(markup).toContain(`data-status="${status}"`);
    expect(markup).toContain("OpenAI");
    expect(markup).toContain("2 attempts");
  });

  it("composes the full shell without owning application state", () => {
    const markup = renderToStaticMarkup(
      <AppShell
        details={
          <RunDetailsPanel
            details={[{ label: "Status", value: "Fallback" }]}
            steps={[
              { provider: "OPENAI", status: "failed" },
              { provider: "ANTHROPIC", status: "success" },
              { provider: "GEMINI", status: "skipped" },
            ]}
          />
        }
        detailsState="visible"
        sidebar={<NavigationSidebar conversations={[]} state="collapsed" />}
        sidebarState="collapsed"
        topBar={<TopBar actions={<PrimaryGlassButton>Details</PrimaryGlassButton>} title="AI Chat Console" />}
      >
        <MessageViewport>
          <UserMessageBubble>Hello</UserMessageBubble>
          <AssistantMessageCard>Hi there</AssistantMessageCard>
        </MessageViewport>
        <PromptComposer onChange={vi.fn()} onSubmit={vi.fn()} sendIcon="↑" value="Prompt" />
      </AppShell>,
    );

    expect(markup).toContain('data-sidebar="collapsed"');
    expect(markup).toContain('data-details="visible"');
    expect(markup).toContain("Run details");
    expect(markup).toContain("Prompt");
  });

  it("omits hidden run details from the accessible tree", () => {
    const markup = renderToStaticMarkup(
      <AppShell details={<div>Private details</div>} detailsState="hidden" sidebar={<nav />} topBar={<header />}>
        <MessageViewport />
      </AppShell>,
    );

    expect(markup).not.toContain("Private details");
    expect(markup).toContain("Start a conversation");
  });
});
