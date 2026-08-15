import type { ReactNode } from "react";
import styles from "./components.module.css";

export interface UserMessageBubbleProps {
  children: ReactNode;
  label?: ReactNode;
}

export function UserMessageBubble({ children, label = "You" }: UserMessageBubbleProps) {
  return (
    <article className={styles.userBubble}>
      <div className={styles.messageLabel}>{label}</div>
      <div className={styles.messageBody}>{children}</div>
    </article>
  );
}

export interface AssistantMessageCardProps {
  actions?: ReactNode;
  children: ReactNode;
  label?: ReactNode;
  pending?: boolean;
}

export function AssistantMessageCard({ actions, children, label = "Assistant", pending = false }: AssistantMessageCardProps) {
  return (
    <article aria-busy={pending} className={styles.assistantCard}>
      <div className={styles.messageLabel}>{label}</div>
      <div className={styles.messageBody}>{children}</div>
      {actions && <div className={styles.messageActions}>{actions}</div>}
    </article>
  );
}

export interface MessageViewportProps {
  children?: ReactNode;
  emptyState?: ReactNode;
  loading?: boolean;
}

export function MessageViewport({ children, emptyState = "Start a conversation", loading = false }: MessageViewportProps) {
  const hasMessages = children !== undefined && children !== null;

  return (
    <section aria-busy={loading} aria-label="Messages" className={styles.messageViewport}>
      <div className={styles.messageViewportInner}>
        {loading && !hasMessages ? <div className={styles.viewportState}>Loading…</div> : null}
        {!loading && !hasMessages ? <div className={styles.viewportState}>{emptyState}</div> : children}
      </div>
    </section>
  );
}
