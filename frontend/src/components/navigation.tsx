import type { ReactNode } from "react";
import { PrimaryGlassButton } from "./buttons";
import styles from "./components.module.css";

export interface ConversationSummary {
  id: string;
  preview?: string;
  timestamp?: string;
  title: string;
}

export interface ConversationRowProps extends ConversationSummary {
  disabled?: boolean;
  onSelect?: (id: string) => void;
  selected?: boolean;
}

export function ConversationRow({ id, title, preview, timestamp, disabled, onSelect, selected = false }: ConversationRowProps) {
  return (
    <button
      aria-current={selected ? "page" : undefined}
      className={`${styles.buttonReset} ${styles.conversationRow}`}
      data-selected={selected}
      disabled={disabled}
      onClick={() => onSelect?.(id)}
      type="button"
    >
      <span className={styles.conversationHeader}>
        <span className={styles.conversationTitle}>{title}</span>
        {timestamp && <time className={styles.conversationTime}>{timestamp}</time>}
      </span>
      {preview && <span className={styles.conversationPreview}>{preview}</span>}
    </button>
  );
}

export interface NavigationSidebarProps {
  brand?: ReactNode;
  brandMark?: ReactNode;
  conversations: readonly ConversationSummary[];
  footer?: ReactNode;
  newConversationIcon?: ReactNode;
  onNewConversation?: () => void;
  onSelectConversation?: (id: string) => void;
  selectedConversationId?: string;
  state?: "expanded" | "collapsed";
}

export function NavigationSidebar({
  brand = "AI Console",
  brandMark,
  conversations,
  footer,
  newConversationIcon,
  onNewConversation,
  onSelectConversation,
  selectedConversationId,
  state = "expanded",
}: NavigationSidebarProps) {
  const collapsed = state === "collapsed";

  return (
    <nav aria-label="Conversations" className={styles.sidebar} data-state={state}>
      <div className={styles.sidebarHeader}>
        {brandMark}
        {!collapsed && <span className={styles.sidebarBrand}>{brand}</span>}
      </div>
      <PrimaryGlassButton aria-label={collapsed ? "New conversation" : undefined} leadingIcon={newConversationIcon} onClick={onNewConversation}>
        {collapsed ? "+" : "New conversation"}
      </PrimaryGlassButton>
      <div className={styles.conversationList}>
        {collapsed
          ? conversations.slice(0, 6).map((conversation) => (
              <button
                aria-label={conversation.title}
                className={`${styles.buttonReset} ${styles.collapsedNavItem}`}
                key={conversation.id}
                onClick={() => onSelectConversation?.(conversation.id)}
                type="button"
              >
                {conversation.title.slice(0, 1).toUpperCase()}
              </button>
            ))
          : conversations.map((conversation) => (
              <ConversationRow
                {...conversation}
                key={conversation.id}
                onSelect={onSelectConversation}
                selected={conversation.id === selectedConversationId}
              />
            ))}
      </div>
      {footer && <div className={styles.sidebarFooter}>{footer}</div>}
    </nav>
  );
}

export interface TopBarProps {
  actions?: ReactNode;
  leading?: ReactNode;
  title: ReactNode;
}

export function TopBar({ actions, leading, title }: TopBarProps) {
  return (
    <header className={styles.topBar}>
      <div className={styles.topBarGroup}>
        {leading}
        <div className={styles.topBarTitle}>{title}</div>
      </div>
      {actions && <div className={styles.topBarGroup}>{actions}</div>}
    </header>
  );
}
